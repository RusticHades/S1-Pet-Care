<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$id_mascota = isset($_GET['id_mascota']) ? intval($_GET['id_mascota']) : 0;

$sql = "SELECT * FROM MASCOTAS WHERE id_mascota = $id_mascota";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $mascota = $result->fetch_assoc();
    // Convertir nulls a strings vacíos
    foreach ($mascota as $key => $value) {
        if ($value === null) {
            $mascota[$key] = "";
        }
    }
    echo json_encode($mascota);
} else {
    echo json_encode(["error" => "Mascota no encontrada"]);
}

$conn->close();
?>