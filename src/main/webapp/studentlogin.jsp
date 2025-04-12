<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Login studente</title>
        <link rel="stylesheet" href="css/common.css">
        <link rel="stylesheet" href="css/studentlogin.css">
    </head>
    <body class="roboto-serif">
        <div class="container m">
            <h1 class="fw-m">Area Studente</h1>
            <form class="m" action="login-servlet" method="post">
                <label for="freshmanNumber">Matricola</label>
                <input class="xxs-h fs-m" type="text" name="freshmanNumber" id="freshmanNumber"/>

                <label for="password">Password</label>
                <input class="min-xxs xxs-h fs-m" type="password" name="password" id="password"/>

                <button class="fs-l fw-m xs-h m-w btn-submit trans" type="submit" value="Login">Login</button>

                <a href="teacherlogin.jsp">Sei un docente?</a>
            </form>
        </div>
    </body>
</html>