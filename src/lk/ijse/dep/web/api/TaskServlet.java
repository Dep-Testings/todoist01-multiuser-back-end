package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.dep.web.model.Task;
import lk.ijse.dep.web.model.User;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Lucky Prabath <lucky.prabath94@gmail.com>
 * @since : 2020-12-30
 **/
@WebServlet(name = "TaskServlet", urlPatterns = "/tasks")
public class TaskServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        BasicDataSource cp =(BasicDataSource ) getServletContext().getAttribute("cp");

        try {
            Jsonb jsonb = JsonbBuilder.create(); //entry point
            Task task = jsonb.fromJson(request.getReader(), Task.class);
            Connection connection = cp.getConnection();
            /*validate*/
            if(task.getTask_id() == null || task.getTask_title() == null || task.getPriority() == null || task.getUser_id() == null){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!task.getTask_id().matches("T\\d{3}") || task.getTask_title().trim().isEmpty() || task.getPriority().trim().isEmpty()
                    || task.getUser_id().trim().isEmpty()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Task VALUES (?,?,?,?)");
            pstm.setString(1, task.getTask_id());
            pstm.setString(2, task.getTask_title());
            pstm.setString(3, task.getPriority());
            pstm.setString(4, task.getUser_id());

            if(pstm.executeUpdate()>0){
                response.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        } catch (JsonbException exp){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("task_id");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        response.setContentType("application/json");
        try (Connection connection = cp.getConnection()){
            PrintWriter out = response.getWriter();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Task" + ((id != null) ? "WHERE task_id=?" : ""));
            if (id != null){
                pstm.setObject(1, id);
            }
            ResultSet rst = pstm.executeQuery();
            List<Task> taskList = new ArrayList<>();
            while (rst.next()){
                id= rst.getString(1);
                String task_title = rst.getString(2);
                String priority = rst.getString(3);
                String user_id = rst.getString(4);
                taskList.add(new Task(id, task_title, priority, user_id));
            }

            if( id != null && taskList.isEmpty()){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(taskList));
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("task_id");
        if (id == null || !id.matches("T\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            Jsonb jsonb = JsonbBuilder.create();  //entry point
            Task task = jsonb.fromJson(req.getReader(), Task.class);

            /*validate*/
            if( task.getTask_title() == null || task.getPriority() == null || task.getUser_id() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if ( task.getTask_title().trim().isEmpty() || task.getPriority().trim().isEmpty() || task.getUser_id().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Task WHERE task_id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE Task SET task_title=?, priority=?, user_id=? WHERE task_id=?");
                pstm.setObject(1, task.getTask_title());
                pstm.setObject(2, task.getPriority());
                pstm.setObject(3, task.getUser_id());
                pstm.setObject(4, id);
                if (pstm.executeUpdate() > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else{
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (JsonbException exp){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("task_id");
        if (id == null || !id.matches("T\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            PreparedStatement pstm = connection.prepareStatement("SELECT  * FROM Task WHERE task_id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM Task WHERE task_id=?");
                pstm.setObject(1, id);
                boolean success = pstm.executeUpdate() > 0;
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLIntegrityConstraintViolationException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException throwables) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }
    }
}
