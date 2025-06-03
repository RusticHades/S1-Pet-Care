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
$alergia = $_POST['alergia'];
$severidad = $_POST['severidad'];
$notas = $_POST['notas'];

$sql = "INSERT INTO alergias (id_mascota, alergia, severidad, notas, fecha_registro)
        VALUES (?, ?, ?, ?, NOW())";
$stmt = $conn->prepare($sql);
$stmt->bind_param("isss", $id_mascota, $alergia, $severidad, $notas);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "No se pudo agregar"]);
}
$conn->close();
?>
