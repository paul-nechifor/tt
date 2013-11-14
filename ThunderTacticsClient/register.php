<?php 
if (isset($_POST['register'])){
	$username = $_POST["name"];
	$password = $_POST["pass"];
}else{
	$username="";
	$password="";
}
?>
<html>
<head>
<title>
	Register to ThunderTactics
</title>
<style type="text/css">
	body{
		background: url("resources/img/Image.JPEG") no-repeat;
		background-size: cover;
	}
	form{
		padding: 10px;
		width: 355px; 
		margin: auto;
		background-color: #222;
		position: relative;
	}
	table{
		color: white;
    	table-layout: fixed;
	}
	input[type=submit],input[type=button]{
		border: 1px solid #a33;
		background-color: #922;
		color: white;
	}
</style>
</head>
<body>
<form method="post">
	<input type="hidden" value="0" name="body"/>
	<input type="hidden" value="0" name="clothes"/>
	<table>
		<tr>
			<td>Username:</td>
			<td><input type="text" name="name" value="<?php echo htmlentities($username);?>" tabindex="1"/></td>
			<td rowspan="3" style="text-align:center">
				<table>
				<tr><td></td><td style="text-align:center">
				<img src="resources/avatars/swordman.jpg" id="avatar"/></td>
				</tr>
				<tr>
					<td>Body</td>
					<td>
						<input type="button" onclick="leftBody()" value="&lt;"/>
						<input type="button" onclick="leftBody()" value="&gt;"/>
					</td>
				</tr><tr>
					<td>Clothes</td>
					<td>
						<input type="button" onclick="leftClothes()" value="&lt;"/>
						<input type="button" onclick="rightClothes()" value="&gt;"/>
					</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="pass" value="<?php echo htmlentities($password);?>" tabindex="2"/></td>
		</tr>
		<tr>
			<td colspan=2 style="width:230px">
				<input type="submit" name="register" value="Register"/><br/>
				<?php 
					if (isset($_POST['register'])){
						if(!preg_match("/^[a-zA-Z]([a-zA-Z0-9-.'])+$/",$username)){
							echo "Username is invalid or too short.";
						}else if(strlen($password)<6){
							echo "Password is too short.";
						}
						else{
							$mysql_host = "localhost";
							$mysql_database = "thundertactics";
							$mysql_user = "admin";
							$mysql_password = "namenlos";
							$conn = mysql_connect($mysql_host,$mysql_user,$mysql_password);
							if(!$conn) echo "Error connecting to database.";
							else{
								$db = mysql_select_db($mysql_database,$conn);
								if(!$db) echo "Error connecting to database.";
								else{
									$username = mysql_real_escape_string($username);
									$sql = "select 1 from account where username='$username'";
									if(mysql_fetch_array(mysql_query($sql))){
										echo "Username already exists";
									}else{
										$password =  strtolower(base64_encode(hash ( "sha256" , $password,true)));
										$body = intval($_POST['body']);
										$clothes = intval($_POST['clothes']);
										$sql = "insert into account(username,password,body,clothes) values('$username','$password',$body,$clothes)";
										$ret = mysql_query($sql);
										if($ret) echo "Your account was created successfully";
										else echo "Unexpected error. Please try again.";
									}
								}
							}
						}
					}
				?>
			</td>
		</tr>
		<tr><td colspan="3">Already have an account? Go to <a href="/">login page</a>.</td></tr>
	</table>
</form>

<script type="text/javascript">
var maxClothes = {0: 7, 1: 6};
var body = <?php echo (isset($_POST['body'])?$_POST['body']:"Math.random()<0.5?0:1");?>;
var clothes = <?php echo (isset($_POST['clothes'])?$_POST['clothes']:"parseInt(Math.random()*10)%maxClothes[body]")?>;
var bodyElem = document.getElementsByName("body")[0];
var clothesElem = document.getElementsByName("clothes")[0];
var imgElem = document.getElementById("avatar");
function leftBody(){
	if(body==0)
		body = 1;
	else
		body=0;
	updateImg();
}
function leftClothes(){
	clothes--;
	if(clothes<0) clothes = maxClothes[body]-1;
	clothesElem.value = clothes;
	updateImg();
}
function rightClothes(){
	clothes++;
	clothes %= maxClothes[body];
	updateImg();
}
function updateImg(){
	var avatar = "resources/avatars/";
	if(body==0){
		if(clothes==0) avatar+= "swordman";
		else avatar+= "swordsman"+clothes;
	}else{
		avatar += "archer" + (clothes>0?clothes:"");
	}
	bodyElem.value = body;
	clothesElem.value = clothes;
	imgElem.src = avatar + ".jpg";
}
window.onresize=function(){
	var form = document.getElementsByTagName("form")[0];
	form.style.top = (document.height/2 - form.offsetHeight/2) + "px";
}
window.onresize();
updateImg();
</script>
</body>
</html>