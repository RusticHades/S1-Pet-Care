<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST");
header("Access-Control-Allow-Headers: Content-Type");

// Configuración de la BD
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$id_usuario = isset($_GET['id_usuario']) ? intval($_GET['id_usuario']) : -1;

$sql = "SELECT id_mascota, nombre, especie, raza,  edad, sexo, foto_mascota  FROM MASCOTAS WHERE id_usuario = $id_usuario";
$result = $conn->query($sql);

$mascotas = array();

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $mascotas[] = $row;
    }
}

$conn->close();

echo json_encode($mascotas);
?>