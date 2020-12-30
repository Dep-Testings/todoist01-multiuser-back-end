package lk.ijse.dep.web.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
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
@WebServlet(name = "UserServlet", urlPatterns = "/users")
public class UserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        BasicDataSource cp =(BasicDataSource ) getServletContext().getAttribute("cp");

        try {
            Jsonb jsonb = JsonbBuilder.create(); //entry point
            User user = jsonb.fromJson(request.getReader(), User.class);
            Connection connection = cp.getConnection();
            /*validate*/
            if(user.getUser_id() == null || user.getUsername() == null || user.getPassword() == null || user.getName() == null){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!user.getUser_id().matches("U\\d{3}") || user.getUsername().trim().isEmpty() || user.getPassword().trim().isEmpty()
                    || user.getName().trim().isEmpty()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO User VALUES (?,?,?,?)");
            pstm.setString(1, user.getUser_id());
            pstm.setString(2, user.getUsername());
            pstm.setString(3, user.getPassword());
            pstm.setString(4, user.getName());

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
        String id = request.getParameter("user_id");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        response.setContentType("application/json");
        try (Connection connection = cp.getConnection()){
            PrintWriter out = response.getWriter();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM User" + ((id != null) ? "WHERE id=?" : ""));
            if (id != null){
                pstm.setObject(1, id);
            }
            ResultSet rst = pstm.executeQuery();
            List<User> userList = new ArrayList<>();
            while (rst.next()){
                id= rst.getString(1);
                String username = rst.getString(2);
                String password = rst.getString(3);
                String name = rst.getString(4);
                userList.add(new User(id, username, password, name));
            }

            if( id != null && userList.isEmpty()){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                Jsonb jsonb = JsonbBuilder.create();
                out.println(jsonb.toJson(userList));
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("user_id");
        if (id == null || !id.matches("U\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            Jsonb jsonb = JsonbBuilder.create();  //entry point
            User user = jsonb.fromJson(req.getReader(), User.class);

            /*validate*/
            if( user.getUsername() == null || user.getPassword() == null || user.getName() == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if ( user.getUsername().trim().isEmpty() || user.getPassword().trim().isEmpty() || user.getName().trim().isEmpty()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM User WHERE user_id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("UPDATE User SET username=?, password=?, name=? WHERE user_id=?");
                pstm.setObject(1, user.getUsername());
                pstm.setObject(2, user.getPassword());
                pstm.setObject(3, user.getName());
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
        String id = req.getParameter("user_id");
        if (id == null || !id.matches("U\\d{3}")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()){
            PreparedStatement pstm = connection.prepareStatement("SELECT  * FROM User WHERE user_id=?");
            pstm.setObject(1, id);
            if(pstm.executeQuery().next()) {
                pstm = connection.prepareStatement("DELETE FROM User WHERE user_id=?");
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
