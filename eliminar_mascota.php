<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
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

// Obtener ID de la mascota
$id_mascota = isset($_POST['id_mascota']) ? intval($_POST['id_mascota']) : 0;

if ($id_mascota <= 0) {
    echo json_encode(['success' => false, 'message' => 'ID inválido']);
    exit;
}

// Eliminar mascota
$sql = "DELETE FROM MASCOTAS WHERE id_mascota = $id_mascota";
$result = $conn->query($sql);

if ($result) {
    echo json_encode(['success' => true]);
} else {
    echo json_encode(['success' => false, 'message' => $conn->error]);
}

$conn->close();
?>