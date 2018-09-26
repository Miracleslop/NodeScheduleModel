package com.demo;

import com.demo.constans.DicReturnType;
import com.demo.constans.DicSocketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * 调度中心
 */
public final class ScheduleSocket {

    private static final Logger log = LoggerFactory.getLogger(ScheduleSocket.class);

    private int defaultReadTime = 3000;

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
    private Map<String, MSSocket> socketMap;

    private Thread polling;


    public ScheduleSocket(final int socket_num, final int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(3600000);
        this.socketMap = new HashMap<>();
        while (this.socketMap.size() < socket_num) {
            Socket accept = serverSocket.accept();
            MSSocket msSocket = new MSSocket(accept);
            accept.setSoTimeout(defaultReadTime);
            this.socketMap.put(msSocket.toString(), new MSSocket(accept));
        }

        /**
         * 轮询每个BUSY状态节点连接，对每个节点监听默认时间，完成任务或者结束socket连接
         * BUSY -> FREE / BUSY -> OVER  /  BUSY -> CLOSE
         */
        Runnable runnable = () -> {
            log.debug("start polling...");
            while (true) {
                //  记录接收数据异常的节点
                int count = 0;
                for (MSSocket msk : socketMap.values()) {
                    try {
                        //  如何socket处于不可用状态，则拜拜
                        if (!msk.isAlive()) {
                            msk.signClose();
                        }
                        if (msk.status.equals(DicSocketStatus.BUSY)) {
                            String rep = "";
                            try {
                                rep = msk.readString();
                                if (rep.startsWith(DicReturnType.SUCCESS.str())) {
                                    msk.completeTask();
                                } else if (rep.startsWith(DicReturnType.OVER.str())) {
                                    msk.signOver();
                                } else if (rep.startsWith(DicReturnType.FAIL.str())) {
                                    log.debug("FAIL socket " + msk.toString() + ", response: " + rep + "     BUSY -> FREE");
                                    msk.completeTask();
                                }
                            } catch (EOFException eof) {
                                log.debug("socket " + msk.toString() + " is closed");
                                msk.signClose();
                                ++count;
                            } catch (SocketException se) {
                                log.warn("socket " + msk.toString() + "  socket lose... :" + se.getMessage());
                                msk.signClose();
                                ++count;
                            } catch (Exception e) {
                                log.warn("socket " + msk.toString() + ", response: " + rep + "  read time out... :" + e.getMessage());
                            }
                        } else if (msk.status.equals(DicSocketStatus.OVER) || msk.status.equals(DicSocketStatus.CLOSE)) {
                            ++count;
                        }
                        sleep(500);
                    } catch (Exception e) {
                        log.error("polling exception: " + e.getMessage());
                    }
                }
                if (count == socketMap.size()) {
                    log.debug("socket of all is over, and polling end");
                    break;
                }
            }
        };
        this.polling = new Thread(runnable, "polling");
        this.polling.start();
    }


    /**
     * 判断所有的socket是否都处于CLOSE(表示已经关闭)
     *
     * @return
     */
    public boolean isAllClose() {
        //  标记是否有socket处于NOT CLOSE状态
        boolean sign = true;
        for (MSSocket clientSocket : socketMap.values()) {
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
            for (MSSocket msk : socketMap.values()) {
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
            if (count == socketMap.size()) {
                log.debug("Don't have effective node");
                throw new NoHaveEffectiveNodeException();
            }
            //  如何为false，则代表没有空闲的节点可以调度
            sleep(2000);
        }
    }

    /**
     * 关闭处于OVER状态的节点
     * OVER -> CLOSE
     */
    public void close() {
        for (MSSocket st : socketMap.values()) {
            if (st.status.equals(DicSocketStatus.OVER)) {
                st.signClose();
                st.close();
                log.debug("close socket: " + st.toString());
            }
        }
    }

    public void safeInterrupt() {
        for (MSSocket st : socketMap.values()) {
            st.signClose();
            try {
                st.close();
            } catch (Exception e) {
                log.warn(st.toString() + " close exception :" + e.getMessage());
            }
        }
        try {
            this.polling.join();
        } catch (InterruptedException e) {
            log.error(this.polling.getName() + " join exception: " + e.getMessage());
        }
    }


}
