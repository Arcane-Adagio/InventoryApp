<?php

/* This is definitely a sloppy way of doing things as I don't really know SQL.
	As I was building this out, I realized that each list within a table should be
	a table of its own to represent M x N relationships, but that would take a lot more
	statements and troubleshooting time that I don't have. So here is my hack job of a database */

/* TO DO: Remove SQL vulnerabilities surrounding sqli statements
		  Refactor code to follow DRY principle
		  There's a problem when deleting account before leaving all groups
*/

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

require 'vendor/autoload.php';
function connect_to_demodb(){
    //specifies the database
	$dbconnection = new PDO('mysql:host=localhost;dbname=demodb','root','');
	return $dbconnection;
}

function connect_to_db(){
    //specifies the database
	$dbconnection = new PDO('mysql:host=localhost;dbname=inventory_app_db','root','');
	return $dbconnection;
}

function connect_to_db_sqli(){
    $servername = "localhost";
	$database = "inventory_app_db";
	$username = "root";
	$password = "";
	$dbconnection = mysqli_connect($servername,$username,$password, $database);
	return $dbconnection;
}

//Slim App is a reference to the SLIM FRAMEWORK
$app = new \Slim\App;


/* When the URL followed by '/api/users/' is entered into the browser
return a JSON for the database */
$app->get('/api/users', function(Request $request, Response $response){
	$sql_query="SELECT * FROM demotable";
	try
	{
		$datab = connect_to_demodb();
		$stmt = $datab->query($sql_query);
		$users = $stmt->fetchAll(PDO::FETCH_OBJ);
		$datab=null;
		echo json_encode($users);
	}
	catch(PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->get('/api/login', function(Request $request, Response $response){
	//example request: http://inventoryapp.com/api/login?username=dev&password=null
	$name = $request->getParam('username');
	$password = $request->getParam('password');
	$sql_query="SELECT * FROM account where USERNAME = '$name' and PASSWORD = '$password'";
	try
	{
		$datab = connect_to_db();
		$stmt = $datab->query($sql_query);
		$users = $stmt->fetchAll(PDO::FETCH_OBJ);
		$datab=null;
		if (Count($users) != 1)
			Respond("Username or Password is incorrect!");
		else{
			RespondWithResult($users, "Login Successful");
		}
	}
	catch(PDOException $e){
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->get('/api', function(Request $request, Response $response){
	print "\nI am alive - API PAGE - InventoryApp\n";
});

$app->get('/', function(Request $request, Response $response){
	print "\nI am alive - HOME PAGE - InventoryApp\n";
});

$app->get('/api/accounts', function(Request $request, Response $response){
	$sql_query="SELECT username, password, inventory FROM account";
	try
	{
		$datab = connect_to_db();
		$stmt = $datab->query($sql_query);
		$users = $stmt->fetchAll(PDO::FETCH_OBJ);
		$datab=null;
		RespondWithResult($users, "Accounts Delivered!");
	}
	catch(PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->get('/api/getInventory', function(Request $request, Response $response){
	//example request: http://inventoryapp.com/api/getInventory?username=dev&password=null
	$name = $request->getParam('username');
	$password = $request->getParam('password');
	$sql_query="SELECT inventory FROM account where USERNAME = '$name' and PASSWORD = '$password'";
	try
	{
		$datab = connect_to_db();
		$stmt = $datab->query($sql_query);
		$users = $stmt->fetchAll(PDO::FETCH_OBJ);
		$datab=null;
		Respond($users[0]->inventory);
	}
	catch(PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/accounts/add', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	
	//check for existing user here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT username FROM account WHERE USERNAME='$name'");
	if(mysqli_num_rows($sql) > 0){
		Respond("User already Exists");
		return;
	}

	//Add user
	$sql_query="INSERT INTO account (username,password)VALUES (:name,:pass)";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':name',$name);
		$stmt->bindParam(':pass',$pass);
		$stmt->execute();
		$datab=null;
		Respond("User Added");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/groups/add', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$gname = $request->getParam('group_name');
	$gpass = $request->getParam('group_password');
	
	//check for existing group here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) > 0){
		VerboselyRespond("false", "Group already Exists");
		return;
	}

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$id = mysqli_fetch_array($sql)['id'];
	$identification = $name.'_'.$id.',';

	//Create group && update user account
	$sql_query="INSERT INTO inventorygroup (group_code,group_name,group_password,owner,members)VALUES (:code,:gname,:pass,:owner,:id)";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':code',$gcode);
		$stmt->bindParam(':gname',$gname);
		$stmt->bindParam(':pass',$gpass);
		$stmt->bindParam(':owner',$name);
		$stmt->bindParam(':id',$identification);
		$stmt->execute();
		$sql_query="UPDATE account SET groups=CASE WHEN groups IS NULL OR groups='' THEN :code ELSE CONCAT_WS('',groups, :code) END WHERE id = '$id'";
		$stmt=$datab->prepare($sql_query);
		$mod_gcode = $gcode.',';
		$stmt->bindParam(':code',$mod_gcode);
		$stmt->execute();
		$datab=null;
		VerboselyRespond("true", "Group Created");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/accounts/changeusername', function (Request $request, Response $response){
	// Example request:
	//localhost/inventoryapp/api/accounts/changeusername?oldusername=name1&newusername=name2&password=nah
	$oldname = $request->getParam('oldusername');
	$newname = $request->getParam('newusername');
	$pass = $request->getParam('password');
	
	//check for existing user here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT * FROM account WHERE USERNAME='$oldname' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) > 0){
		$id = mysqli_fetch_array($sql)['id'];

		//change password
		$sql_query="UPDATE account SET USERNAME = :newname WHERE id = '$id'";
		try
		{
			$datab=connect_to_db();
			$stmt=$datab->prepare($sql_query);
			$stmt->bindParam(':newname',$newname);
			$stmt->execute();
			$datab=null;
			Respond("Username has been changed");
		}
		catch (PDOException $e)
		{
			echo '{"error":{"text":'.$e->getMessage().'}';
		}
	}
	else{
		Respond("Username or Password is incorrect");
	}
});

$app->post('/api/groups/changegroupname', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$name = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$gpass = $request->getParam('new_group_name');
	
	//check to make sure the request is coming from the owner

	echo "To Do ...";
});

$app->post('/api/accounts/saveinventory', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$inventory = $request->getParam('inventory');
	$modifiedinventory = str_replace('"', "'", $inventory);
	
	//check for existing user here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT * FROM account WHERE USERNAME='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) > 0){
		$id = mysqli_fetch_array($sql)['id'];

		//change inventory
		$sql_query="UPDATE account SET INVENTORY = :inventory WHERE id = '$id'";
		try
		{
			$datab=connect_to_db();
			$stmt=$datab->prepare($sql_query);
			$stmt->bindParam(':inventory', $modifiedinventory);
			$stmt->execute();
			$datab=null;
			Respond("Inventory Saved!");
		}
		catch (PDOException $e)
		{
			echo '{"error":{"text":'.$e->getMessage().'}';
		}
	}
	else{
		Respond("Username or Password is incorrect");
	}
});

$app->post('/api/groups/saveinventory', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$inventory = $request->getParam('inventory');
	$modifiedinventory = str_replace('"', "'", $inventory);

	
	//check if user is in group
	//eventually :: check if user has write permissions



});

$app->post('/api/accounts/changepassword', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$oldpass = $request->getParam('oldpassword');
	$newpass = $request->getParam('newpassword');
	
	//check for existing user here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT * FROM account WHERE USERNAME='$name' and PASSWORD = '$oldpass'");
	if(mysqli_num_rows($sql) > 0){
		$id = mysqli_fetch_array($sql)['id'];

		//change password
		$sql_query="UPDATE account SET PASSWORD = :newpass WHERE id = '$id'";
		try
		{
			$datab=connect_to_db();
			$stmt=$datab->prepare($sql_query);
			$stmt->bindParam(':newpass',$newpass);
			$stmt->execute();
			$datab=null;
			Respond("User password has been changed");
		}
		catch (PDOException $e)
		{
			echo '{"error":{"text":'.$e->getMessage().'}';
		}
	}
	else{
		echo Respond("Username or Password is incorrect");
	}
});

$app->post('/api/groups/changepassword', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$name = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$newgpass = $request->getParam('new_group_password');
	
	//check to make sure the request is coming from the owner

	echo "To Do ...";
});

$app->post('/api/accounts/remove', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$id = $row["id"];
	$identification = $name.'_'.$id.',';

	try
	{
		//Find all groups user is current in
		$result = mysqli_query(connect_to_db_sqli(), "SELECT id, owner, members FROM inventorygroup WHERE LOCATE('$identification', members) != 0");
		while($row = mysqli_fetch_array($result)){
			$group_id = $row['id'];
			$members = $row["members"];
			$group_owner = $row['owner'];

			//remove self from group
			$sql_query="UPDATE inventorygroup SET members = REPLACE(members, :id, '') WHERE id = '$group_id'";
			$datab=connect_to_db();
			$stmt=$datab->prepare($sql_query);
			$stmt->bindParam(':id',$identification);
			$stmt->execute();

			//if the owner
			if($group_owner == $name){
				//and there are others in the group, appoint someone else as owner
				if (str_contains($members, "_")){
					$target_username = substr($members, 0, strpos($members, '_'));
					$result = mysqli_query(connect_to_db_sqli(), "UPDATE inventorygroup SET owner = $target_username WHERE id = '$group_id'");
				}
				else{ //delete group
					$sql_query="DELETE FROM inventorygroup where group_code = :code";
					$datab=connect_to_db();
					$stmt=$datab->prepare($sql_query);
					$stmt->bindParam(':code',$gcode);
					$stmt->execute();
				}
			}
		}

		//Remove account from database
		$sql_query="DELETE FROM account where USERNAME = :name and PASSWORD = :pass";
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':name',$name);
		$stmt->bindParam(':pass',$pass);
		$stmt->execute();
		$datab=null;
		Respond("User Deleted");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/groups/remove', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$gpass = $request->getParam('group_password');
	
	//check for existing group here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT owner FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$group_owner = mysqli_fetch_array($sql)['owner'];

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$username = mysqli_fetch_array($sql)['username'];

	//Condition
	if($group_owner != $username){
		VerboselyRespond("false", "Only the owner can delete the group");
		return;
	}

	//Delete group && update user accounts
	$sql_query="DELETE FROM inventorygroup where group_code = :code";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':code',$gcode);
		$stmt->execute();
		//slow query, update [groups in account] and [members in inventorygroup] to be a table of it's own
		$sql_query="UPDATE account SET groups= REPLACE(groups, :code, '') WHERE LOCATE(:code, groups) != 0";
		$stmt=$datab->prepare($sql_query);
		$mod_gcode = $gcode.',';
		$stmt->bindParam(':code',$mod_gcode);
		$stmt->execute();
		$datab=null;
		VerboselyRespond("true", "Group Deleted");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/groups/join', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$gpass = $request->getParam('group_password');
	
	//check if group exists
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT group_password, members FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$group_password = $row['group_password'];
	$members = $row['members'];

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$id = mysqli_fetch_array($sql)['id'];
	$identification = $name.'_'.$id.',';

	//Only let user join if password is correct
	if($group_password != $gpass){
		VerboselyRespond("false", "Incorrect Password");
		return;
	}

	//Only let user join if user is not in group already
	if(str_contains($members, $identification)){
		VerboselyRespond("false", "Already in Group");
		return;
	}

	//TO DO:: Check if not banned from group

	//Join Group and Update User Account
	$sql_query="UPDATE inventorygroup SET members=CASE WHEN members IS NULL OR members='' THEN :id ELSE CONCAT(members, :id) END  WHERE group_code ='$gcode'";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':id',$identification);
		$stmt->execute();
		//slow query, update [groups in account] and [members in inventorygroup] to be a table of it's own
		$sql_query="UPDATE account SET groups=CASE WHEN groups IS NULL OR groups='' THEN :code ELSE CONCAT_WS('',groups, :code) END WHERE id = '$id'";
		$stmt=$datab->prepare($sql_query);
		$mod_gcode = $gcode.',';
		$stmt->bindParam(':code',$mod_gcode);
		$stmt->execute();
		//get inventory
		$sql_query="SELECT group_code, group_name, owner, members, inventory FROM inventorygroup WHERE group_code = :code";
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':code',$gcode);
		$stmt->execute();
		$group_info = $stmt->fetchAll(PDO::FETCH_OBJ);
		$datab=null;
		VerboselyRespondWithResult("true", $group_info, "Group Joined");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/groups/leave', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	
	//check if group exists
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT group_password, members FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$group_password = $row['group_password'];
	$members = $row['members'];

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$id = mysqli_fetch_array($sql)['id'];
	$identification = $name.'_'.$id.',';

	//Only let user leave if user is in group 
	if(!str_contains($members, $identification)){
		VerboselyRespond("false", "You can't leave what you weren't apart of, playa");
		return;
	}

	//Leave Group and Update User Account
	$sql_query="UPDATE inventorygroup SET members = REPLACE(members, :id, '') WHERE group_code ='$gcode'";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':id',$identification);
		$stmt->execute();
		//slow query, update [groups in account] and [members in inventorygroup] to be a table of it's own
		$sql_query="UPDATE account SET groups= REPLACE(groups, :code, '') WHERE id = '$id'";
		$stmt=$datab->prepare($sql_query);
		$mod_gcode = $gcode.',';
		$stmt->bindParam(':code',$mod_gcode);
		$stmt->execute();
		$datab=null;
		VerboselyRespond("true", "Group Left");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->get('/api/groups/getInventory', function(Request $request, Response $response){
	//example request: inventoryapp.com/api/groups/getInventory?username=77&group_code=tube456&password=777
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');

	//check if group exists
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT inventory, members FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$inventory = $row['inventory'];
	$members = $row['members'];

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$id = mysqli_fetch_array($sql)['id'];
	$identification = $name.'_'.$id.',';

	//check if user is in group
	if(!str_contains($members, $identification)){
		VerboselyRespond("false", "Unauthorized Access: User is not in Group");
		return;
	}

	//Give user the goods
	VerboselyRespond("true",$inventory);
});

$app->post('/api/groups/saveInventory', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	$inventory = $request->getParam('inventory');
	$modifiedinventory = str_replace('"', "'", $inventory);
	
	//check if group exists
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT group_password, members FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$group_password = $row['group_password'];
	$members = $row['members'];

	//Authenticate user
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$id = mysqli_fetch_array($sql)['id'];
	$identification = $name.'_'.$id.',';

	//check if user is in group
	if(!str_contains($members, $identification)){
		VerboselyRespond("false", "Unauthorized Access: User is not in Group");
		return;
	}

	//Join Group and Update User Account
	$sql_query="UPDATE inventorygroup SET INVENTORY = :inventory  WHERE group_code ='$gcode'";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':inventory', $modifiedinventory);
		$stmt->execute();
		$datab=null;
		VerboselyRespond("true","Inventory Saved!");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

$app->post('/api/groups/kick', function (Request $request, Response $response){
	$name = $request->getParam('username');
	$target_username = $request->getParam('target_username');
	$pass = $request->getParam('password');
	$gcode = $request->getParam('group_code');
	
	//check for existing group here
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT owner, members, banned FROM inventorygroup WHERE group_code ='$gcode'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Group does not exist");
		return;
	}
	$row = mysqli_fetch_array($sql);
	$group_owner = $row['owner'];
	$current_members = $row['members'];
	$banned_users = $row['banned'];


	//Authenticate users
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id, username FROM account WHERE USERNAME ='$name' and PASSWORD = '$pass'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "Authentication Failed");
		return;
	}
	$username = mysqli_fetch_array($sql)['username'];

	//Check if target exists
	$sql = mysqli_query(connect_to_db_sqli(), "SELECT id FROM account WHERE USERNAME ='$target_username'");
	if(mysqli_num_rows($sql) == 0){
		VerboselyRespond("false", "User does not exist");
		return;
	}
	$target_id = mysqli_fetch_array($sql)['id'];
	$target_identification = $target_username.'_'.$target_id.',';


	//Check if operation is being performed by the owner of the group
	if($group_owner != $username){
		VerboselyRespond("false", "Only the owner can remove people from the group");
		return;
	}

	//Cant kick someone who isnt in the group
	if(!str_contains($current_members, $target_username)){
		VerboselyRespond("false", "User is not in Group");
		return;
	}

	//remove user from group && update user's account
	$sql_query="UPDATE inventorygroup SET members = REPLACE(members, :id, '') WHERE group_code ='$gcode'";
	try
	{
		$datab=connect_to_db();
		$stmt=$datab->prepare($sql_query);
		$stmt->bindParam(':id',$target_identification);
		$stmt->execute();
		//slow query, update [groups in account] and [members in inventorygroup] to be a table of it's own
		$sql_query="UPDATE account SET groups= REPLACE(groups, :code, '') WHERE id = '$target_id'";
		$stmt=$datab->prepare($sql_query);
		$mod_gcode = $gcode.',';
		$stmt->bindParam(':code',$mod_gcode);
		$stmt->execute();
		$datab=null;
		VerboselyRespond("true", "User has been removed from group");
	}
	catch (PDOException $e)
	{
		echo '{"error":{"text":'.$e->getMessage().'}';
	}
});

/* Example of unsafe code */
$app->get('/unsafe/{name}', function(Request $request, Response $response){
    $name = $request->getAttribute('name');
    $response->getBody()->write("Halo, $name!\n");
    return $response;
});

/* Example of safe code */
$app->get('/safe/{name}', function(Request $request, Response $response){
	//code is incomplete
    $name = $request->getParam('name');
	try{
		$response->getBody()->write("Halo, $name!\n");
	}
	catch (PDOException $e){

	}
    return $response;
});

function getVisitorIp() {
	// Look for HTTP_X_FORWARDED_FOR header
	if(!empty($_SERVER['HTTP_X_FORWARDED_FOR'])){
	  $address = $_SERVER['HTTP_X_FORWARDED_FOR'];
	// Look for HTTP_CLIENT_IP header
	}elseif(!empty($_SERVER['HTTP_CLIENT_IP'])){
	  $address = $_SERVER['HTTP_CLIENT_IP'];
	// Get the client's IP address from the REMOTE_ADDR variable
	}else{
	  $address = $_SERVER['REMOTE_ADDR'];
	}
	return $address;
}


function special_json_encode($arr){
	return str_replace('\"', '"', json_encode($arr));
}

function PrepResponse($text){
	$arr1 = array("text"=>$text);
	$result = array("Result"=>$arr1);
	return $result;
}

function Respond($text){
	$arr1 = array("text"=>$text);
	$result = array("Result"=>$arr1);
	echo "[" . special_json_encode($result) . "]"; 
}

function VerboselyRespond($bool, $text){
	$arr = array("succeeded"=>$bool, "text"=>$text);
	$result = array("Result"=>$arr);
	echo "[" . special_json_encode($result) . "]"; 
}

function RespondTernary($condition, $text_if_true, $text_if_false){
	if($condition)
		$arr1 = array("text"=>$text_if_true);
	else
		$arr1 = array("text"=>$text_if_false);
	$result = array("Result"=>$arr1);
	echo special_json_encode($result);
}

function RespondWithResult($result, $text){
	$arr1 = array("text"=>$text);
	$response_text = array("Result"=>$arr1);
	array_push($result, $response_text);
	echo special_json_encode($result);
}

function VerboselyRespondWithResult($bool, $result, $text){
	$arr1 = array("succeeded"=>$bool, "text"=>$text);
	$response_text = array("Result"=>$arr1);
	array_push($result, $response_text);
	echo special_json_encode($result);
}

$app->get('/api/testme', function(Request $request, Response $response){
	//$ress = $_SERVER['REMOTE_ADDR'];
	//echo $ress;
	echo getVisitorIp();
});


//Start the framework to handle REST requests
$app->run();


