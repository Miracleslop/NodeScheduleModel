package com.demo;

import com.demo.constans.DicReturnType;

/**
 * 任务调度中心，负责初始化创建任务序列，并返回当个任务
 */
public final class TaskControl {

    private int per;
    private int total;

    /**
     * 代表当前任务下标
     */
    private int index;
    private int num;


    public TaskControl(int per, int total) {
        this.per = per;
        this.total = total;
        this.index = 0;
        this.num = (int) Math.ceil((double) total / (double) per);
    }

    public boolean hasNext() {
        return this.index < this.num;
    }

    /**
     * 类比list的迭代器next
     *
     * @return 当前任务，下标移到到下一个任务
     */
    public Task next() {
        //  创建任务并返回
        Task task = new Task(DicReturnType.TASK.str() + this.index * this.per + "," + this.per);
        //  移动下标到下一个任务
        ++this.index;
        return task;
    }


    public Task next(String extra) {
        //  创建任务并返回
        Task task = new Task(DicReturnType.TASK.str() + this.index * this.per + "," + this.per + "," + extra);
        //  移动下标到下一个任务
        ++this.index;
        return task;
    }

    @Override
    public String toString() {
        return "TaskControl{" +
                "per=" + per +
                ", total=" + total +
                ", num=" + num +
                '}';
    }
}
