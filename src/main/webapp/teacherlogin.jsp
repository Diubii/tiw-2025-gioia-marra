<%--
  Created by IntelliJ IDEA.
  User: alessandro
  Date: 12/04/25
  Time: 16:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Login docente</title>
  <link rel="stylesheet" href="css/common.css">
  <link rel="stylesheet" href="css/teacherlogin.css">
</head>
<body class="roboto-serif">
  <div class="container m">
    <h1 class="fw-m">Area Docente</h1>
    <form class="m" action="login-servlet" method="post">
      <label for="teacherID">ID docente</label>
      <input class="xxs-h fs-m" type="text" name="teacherID" id="teacherID"/>

      <label for="password">Password</label>
      <input class="min-xxs xxs-h fs-m" type="password" name="password" id="password"/>

      <button class="fs-l fw-m xs-h m-w btn-submit trans" type="submit" value="Login">Login</button>

      <a href="studentlogin.jsp">Sei uno studente?</a>
    </form>
  </div>
</body>
</html>
