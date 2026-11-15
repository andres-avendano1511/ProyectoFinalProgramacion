import java.time.LocalDate;

public class Huesped extends Usuario {
    private String telefono;
    private String rutaFoto;

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento, String telefono, String correo, String password) {
        super(nombre, documento, fechaNacimiento, correo, password);
        this.telefono = telefono;
    }

    @Override
    public String obtenerRolDescripcion() {
        return "Huésped Titular";
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
}