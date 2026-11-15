public interface GestionableEstado {
    boolean cambiarEstado(EstadoReserva nuevoEstado);
    boolean esActiva();
}