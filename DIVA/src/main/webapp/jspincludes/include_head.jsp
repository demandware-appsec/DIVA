<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<base href="<%=request.getContextPath() == "" ? "" : java.net.URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()) %>/" />    
<!-- <link rel="stylesheet" type="text/css" href="css/master.css"> -->
<link rel="stylesheet" href="css/bootstrap.min.css">
<link rel="stylesheet" href="css/styles.css">
<script src="js/jquery-1.11.3.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/scripts.js"></script>


<script type="text/javascript">
    $(document).ready(function(){
        $('[data-toggle="tooltip"]').tooltip();   
    });
</script>



