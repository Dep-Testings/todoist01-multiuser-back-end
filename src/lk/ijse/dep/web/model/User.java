package lk.ijse.dep.web.model;

/**
 * @author : Lucky Prabath <lucky.prabath94@gmail.com>
 * @since : 2020-12-30
 **/
public class User {
    private String user_id;
    private String username;
    private String password;
    private String name;

    public User() {
    }

    public User(String user_id, String username, String password, String name) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
