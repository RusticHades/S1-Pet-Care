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
$dosis = $_POST['dosis'];
$frecuencia = $_POST['frecuencia'];
$motivo = $_POST['motivo'];
$fecha_inicio = $_POST['fecha_inicio'];
$fecha_fin = $_POST['fecha_fin'];
$notas = $_POST['notas'];

$sql = "INSERT INTO medicamentos (id_mascota, nombre, dosis, frecuencia, motivo, fecha_inicio, fecha_fin, notas, fecha_registro)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
$stmt = $conn->prepare($sql);
$stmt->bind_param("isssssss", $id_mascota, $nombre, $dosis, $frecuencia, $motivo, $fecha_inicio, $fecha_fin, $notas);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "No se pudo agregar"]);
}
$conn->close();
?>