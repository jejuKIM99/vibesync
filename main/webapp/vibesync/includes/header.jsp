<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html lang="ko" color-theme="${sessionScope.theme != null ? sessionScope.theme : 'light'}">
<head>
<script defer src="./js/script.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/vibesync/css/sidebar.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/vibesync/css/style.css">