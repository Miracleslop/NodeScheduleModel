package com.demo;

public interface TaskExecutor {

    /**
     * @param taskData 任务数据
     * @return 返回执行是否成功
     */
    boolean handle(String taskData);
}
