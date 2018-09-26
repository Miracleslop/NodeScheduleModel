package com.demo;

@FunctionalInterface
public interface TaskExecutor {

    /**
     * @param taskData 任务数据
     * @return 返回任务执行结果：optSuc 是否成功, reponse:任务数据，msg:异常信息
     */
    TaskExecuteReturn handle(String taskData);
}
