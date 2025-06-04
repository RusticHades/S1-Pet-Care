<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Conexión fallida"]);
    exit;
}

$id_mascota = $_POST['id_mascota'];
$nombre = $_POST['nombre'];
$fecha_aplicacion = $_POST['fecha_aplicacion'];
$fecha_proximo_refuerzo = $_POST['fecha_proximo_refuerzo'];
$veterinario = $_POST['veterinario'];
$notas = $_POST['notas'];

$sql = "INSERT INTO vacunas (id_mascota, nombre, fecha_aplicacion, fecha_proximo_refuerzo, veterinario, notas, fecha_registro)
        VALUES (?, ?, ?, ?, ?, ?, NOW())";
$stmt = $conn->prepare($sql);
$stmt->bind_param("isssss", $id_mascota, $nombre, $fecha_aplicacion, $fecha_proximo_refuerzo, $veterinario, $notas);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "No se pudo agregar"]);
}
$conn->close();
?>