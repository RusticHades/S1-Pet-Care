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
    die(json_encode(["error" => "Connection failed: " . $conn->connect_error]));
}

$id_mascota = isset($_GET['id_mascota']) ? intval($_GET['id_mascota']) : 0;

if ($id_mascota <= 0) {
    die(json_encode(["error" => "ID de mascota inválido"]));
}

// Consulta mejorada con manejo de errores
try {
    $stmt = $conn->prepare("SELECT * FROM MASCOTAS WHERE id_mascota = ?");
    $stmt->bind_param("i", $id_mascota);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $mascota = $result->fetch_assoc();
        
        // Formatear datos para mejor visualización
        $mascota['edad'] = $mascota['edad'] . ' años';
        $mascota['peso'] = $mascota['peso'] . ' kg';
        $mascota['esterilizado'] = $mascota['esterilizado'] == 1 ? 'Sí' : 'No';
        
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
} catch (Exception $e) {
    echo json_encode(["error" => "Error en la consulta: " . $e->getMessage()]);
} finally {
    $stmt->close();
    $conn->close();
}
?>