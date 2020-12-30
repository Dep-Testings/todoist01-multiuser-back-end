package lk.ijse.dep.web.model;

/**
 * @author : Lucky Prabath <lucky.prabath94@gmail.com>
 * @since : 2020-12-30
 **/
public class Task {
    private String task_id;
    private String task_title;
    private String priority;
    private String user_id;

    public Task() {
    }

    public Task(String task_id, String task_title, String priority, String user_id) {
        this.task_id = task_id;
        this.task_title = task_title;
        this.priority = priority;
        this.user_id = user_id;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getTask_title() {
        return task_title;
    }

    public void setTask_title(String task_title) {
        this.task_title = task_title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "task_id='" + task_id + '\'' +
                ", task_title='" + task_title + '\'' +
                ", priority='" + priority + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
