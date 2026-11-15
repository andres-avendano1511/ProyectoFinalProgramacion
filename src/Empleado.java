import java.time.LocalDate;

public class Empleado extends Usuario {
    private boolean esAdministrador;
    private boolean activo;

    public Empleado(String nombre, String documento, LocalDate fechaNacimiento, String correo, String password, boolean esAdministrador) {
        super(nombre, documento, fechaNacimiento, correo, password);
        this.esAdministrador = esAdministrador;
        this.activo = true;
    }

    @Override
    public String obtenerRolDescripcion() {
        return esAdministrador ? "Administrador del Alojamiento" : "Recepcionista";
    }

    public boolean isEsAdministrador() { return esAdministrador; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}