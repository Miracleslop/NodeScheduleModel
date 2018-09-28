package com.demo;

import com.demo.constans.DicReturnType;
import com.demo.constans.DicSocketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/**
 * 调度中心
 */
public final class ScheduleSocket {

    private static final Logger log = LoggerFactory.getLogger(ScheduleSocket.class);

    /**
     * 每个socket默认读取等待时间
     */
    private int defaultReadTime = 3000;

    /**
     * 最大线程数
     */
    private int maxThreadNum = 10;

    /**
     * 最小分片数
     */
    private int minSegmentNum = 4;

    /**
     * 基于基本的socket连接类，扩展为在服务端注册的节点
     */
    class MSSocket extends MSocket {

        DicSocketStatus status;

        MSSocket(Socket socket) throws IOException {
            super(socket);
            status = DicSocketStatus.FREE;
        }


        void allotTask(Task task) throws IOException {
            this.writeString(task.data());
            status = DicSocketStatus.BUSY;
        }

        void completeTask() {
            this.status = DicSocketStatus.FREE;
        }

        void signOver() {
            status = DicSocketStatus.OVER;
        }

        void signClose() {
            this.status = DicSocketStatus.CLOSE;
        }

    }

    /**
     * 记录节点
     */
    private List<MSSocket> socketList;

    /**
     * 当前分配任务的下标
     */
    private int allot_in = 0;

    /**
     * 轮询线程池
     */
    private ExecutorService pollingPool;

    /**
     * 单例
     */
    private static ScheduleSocket instance;

    public static ScheduleSocket getInstance(final int socket_num, final int port) throws IOException {
        if (instance == null) {
            instance = new ScheduleSocket(socket_num, port);
        }
        return instance;
    }


    private ScheduleSocket(final int socket_num, final int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(3600000);
        this.socketList = new ArrayList<>(socket_num);
        //  初始化连接，对接node
        while (this.socketList.size() < socket_num) {
            Socket accept = serverSocket.accept();
            MSSocket msSocket = new MSSocket(accept);
            accept.setSoTimeout(defaultReadTime);
            this.socketList.add(msSocket);
//            log.info(msSocket.toString() + " already connect...");
        }

        //  计算所需轮询的线程数，并启动轮询线程
        int size = this.socketList.size();
        pollingPool = Executors.newFixedThreadPool(maxThreadNum);
        int cal_perSNum = (int) Math.ceil((double) size / (double) maxThreadNum);
        int perSNum = cal_perSNum < minSegmentNum ? minSegmentNum : cal_perSNum;
        int threadNum = 0;
        for (int i = 0; i < size; i += perSNum) {
            List<MSSocket> t_msSockets = this.socketList.subList(i, i + Math.min(size - i, perSNum));
            PollingNodeThread pollingNodeThread = new PollingNodeThread(t_msSockets);
            pollingPool.execute(pollingNodeThread);
            ++threadNum;
        }
        log.info(" --socket num: " + size + " --thread num: " + threadNum + " --poll num of per thread: " + perSNum);
        //  完成线程池的部署，关闭线程线程池入口
        pollingPool.shutdown();

    }

//    public static void main(String[] args) {
//        int size = 36;
//        int maxThreadNum = 10;
//        System.out.println((int) Math.ceil((double) size / (double) maxThreadNum));
//        int cal_perSNum = (int) Math.ceil((double) size / (double) maxThreadNum);
//        System.out.println(cal_perSNum);
//        int perSNum = cal_perSNum < 5 ? 5 : cal_perSNum;
//        System.out.println(perSNum);
//        int threadNum = 0;
//        for (int i = 0; i < size; i += perSNum) {
//            ++threadNum;
//        }
//        System.out.println(threadNum);
//    }


    /**
     * 判断所有的socket是否都处于CLOSE(表示已经关闭)
     *
     * @return
     */
    public boolean isAllNodeClose() {
        //  标记是否有socket处于NOT CLOSE状态
        boolean sign = true;
        for (MSSocket clientSocket : socketList) {
            if (!clientSocket.status.equals(DicSocketStatus.CLOSE)) {
                sign = false;
                break;
            }
        }
        return sign;
    }

    /**
     * 分配单个任务到空闲的节点中，并执行
     * FREE -> BUSY
     *
     * @param task 需要执行的任务
     * @return 返回接收该任务socket唯一识别
     */
    public String allotTask(Task task) throws InterruptedException, NoHaveEffectiveNodeException {
        //  计数OVER、CLOSE状态或者分配任务异常的节点
        while (true) {
            int count = 0;
            for (; allot_in < this.socketList.size(); ++allot_in) {
                MSSocket msk = this.socketList.get(allot_in);
                if (msk.status.equals(DicSocketStatus.FREE)) {
                    try {
                        msk.allotTask(task);
                        return msk.toString();
                    } catch (Exception e) {
                        log.warn("allocation fail, node: " + msk.toString() + " Exception: " + e.getMessage());
                        ++count;
                    }
                } else if (msk.status.equals(DicSocketStatus.CLOSE) || msk.status.equals(DicSocketStatus.OVER)) {
                    ++count;
                }
            }
            allot_in = 0;
            if (count == socketList.size()) {
                log.info("Don't have effective node");
                throw new NoHaveEffectiveNodeException();
            }
            //  如何为false，则代表没有空闲的节点可以调度
            sleep(100);
        }
    }

    /**
     * 关闭处于OVER状态的节点
     * OVER -> CLOSE
     */
    public void closeNode() {
        for (MSSocket st : socketList) {
            if (st.status.equals(DicSocketStatus.OVER)) {
                st.signClose();
                st.close();
//                log.debug("closeNode socket: " + st.toString());
            }
        }
    }

    /**
     * 等待关闭socket，如何时间超过最大等待时间，则强制关闭
     *
     * @param maxWaitSecond 最大等待时间(ms)
     */
    public void waitNodeClose(long maxWaitSecond) {
        try {
            int i = 0;
            while (true) {
                this.closeNode();
                if (this.isAllNodeClose()) {
                    break;
                }
                if (i >= maxWaitSecond) {
                    throw new Exception("wait closeNode fail and safely force closeNode ");
                }
                try {
                    String s = this.allotTask(new Task(DicReturnType.OVER.str()));
//                    log.debug(s + " accept task: OVER");
                } catch (NoHaveEffectiveNodeException e) {
                    log.warn("NoHaveEffectiveNodeException");
                }
//                i += 1;
//                sleep(10);
            }
        } catch (Exception e) {
            this.safeInterrupt();
        }
    }

    public void safeInterrupt() {
        for (MSSocket st : socketList) {
            st.signClose();
            try {
                st.close();
            } catch (Exception e) {
                log.warn(st.toString() + " closeNode exception :" + e.getMessage());
            }
        }
        while (true) {//等待所有任务都结束了继续执行
            try {
                if (pollingPool.isTerminated()) {
                    log.info("all thread of polling pool is terminated ");
                    break;
                }
                sleep(1000);
            } catch (Exception e) {
                log.error(" pollingPool wait closeNode exception: " + e.getMessage(), e);
            }
        }
    }

    public void close() {
        instance.close();
    }


}
