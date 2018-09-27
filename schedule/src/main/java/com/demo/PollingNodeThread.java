package com.demo;

import com.demo.constans.DicReturnType;
import com.demo.constans.DicSocketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * 轮询每个BUSY状态节点连接，对每个节点监听默认时间，完成任务或者结束socket连接
 * BUSY -> FREE / BUSY -> OVER  /  BUSY -> CLOSE
 */
public class PollingNodeThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PollingNodeThread.class);

    private List<ScheduleSocket.MSSocket> socketList;

    public PollingNodeThread(List<ScheduleSocket.MSSocket> socketList) {
        this.socketList = socketList;
    }

    @Override
    public void run() {
        log.debug("start polling...");
        while (true) {
            //  记录接收数据异常的节点
            int count = 0;
            for (ScheduleSocket.MSSocket msk : socketList) {
                try {
                    //  如何socket处于不可用状态，则拜拜
                    if (!msk.isAlive()) {
                        msk.signClose();
                    }
                    if (msk.status.equals(DicSocketStatus.BUSY)) {
                        String rep = "";
                        try {
                            rep = msk.readString();
                            if (rep.startsWith(DicReturnType.OVER.str())) {
                                msk.signOver();
                            } else if (rep.startsWith(DicReturnType.FAIL.str())) {
                                log.warn("FAIL socket " + msk.toString() + ", response: " + rep + "     BUSY -> FREE");
                                msk.completeTask();
                            } else {
                                msk.completeTask();
                            }
                        } catch (SocketTimeoutException ste) {
//                                log.debug("socket " + msk.toString() + " read time out ");
                        } catch (EOFException eof) {
                            log.info("socket " + msk.toString() + " is closed");
                            msk.signClose();
                            ++count;
                        } catch (SocketException se) {
                            log.warn("socket " + msk.toString() + "  socket lose... :" + se.getMessage());
                            msk.signClose();
                            ++count;
                        } catch (Exception e) {
                            log.error("socket " + msk.toString() + ", response: " + rep + "  Exception: " + e.getMessage(), e);
                        }
                    } else if (msk.status.equals(DicSocketStatus.OVER) || msk.status.equals(DicSocketStatus.CLOSE)) {
                        ++count;
                    }
                } catch (Exception e) {
                    log.error("polling exception: " + e.getMessage(), e);
                }
            }
            if (count == socketList.size()) {
                log.info("socket of all is over, and polling end");
                break;
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
