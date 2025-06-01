<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Obtener datos del POST
$id_mascota = isset($_POST['id_mascota']) ? intval($_POST['id_mascota']) : 0;
$nombre = isset($_POST['nombre']) ? $conn->real_escape_string($_POST['nombre']) : '';
$especie = isset($_POST['especie']) ? $conn->real_escape_string($_POST['especie']) : '';
$raza = isset($_POST['raza']) ? $conn->real_escape_string($_POST['raza']) : '';
$fecha_nacimiento = isset($_POST['fecha_nacimiento']) ? $conn->real_escape_string($_POST['fecha_nacimiento']) : null;
$edad = isset($_POST['edad']) ? intval($_POST['edad']) : null;
$sexo = isset($_POST['sexo']) ? $conn->real_escape_string($_POST['sexo']) : '';
$peso = isset($_POST['peso']) ? floatval($_POST['peso']) : null;
$esterilizado = isset($_POST['esterilizado']) ? intval($_POST['esterilizado']) : 0;

// Preparar consulta
$sql = "UPDATE MASCOTAS SET 
        nombre = ?,
        especie = ?,
        raza = ?,
        fecha_nacimiento = ?,
        edad = ?,
        sexo = ?,
        peso = ?,
        esterilizado = ?
        WHERE id_mascota = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("ssssisdsi", 
    $nombre, 
    $especie, 
    $raza, 
    $fecha_nacimiento, 
    $edad, 
    $sexo, 
    $peso, 
    $esterilizado, 
    $id_mascota
);

if ($stmt->execute()) {
    echo json_encode(['success' => true]);
} else {
    echo json_encode(['success' => false, 'error' => $stmt->error]);
}

$stmt->close();
$conn->close();
?>