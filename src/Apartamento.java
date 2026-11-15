import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Apartamento {
    private String codigo;
    private String nombre;
    private int numeroDormitorios;
    private int capacidadMaxima;
    private List<String> caracteristicas;
    private Map<Temporada, Long> tarifasPorTemporada;
    private EstadoApartamento estado;
    private String motivoFueraServicio;
    private boolean activo;

    public Apartamento(String codigo, String nombre, int numeroDormitorios, int capacidadMaxima,
                       List<String> caracteristicas, long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = caracteristicas;
        this.tarifasPorTemporada = new EnumMap<>(Temporada.class);
        this.tarifasPorTemporada.put(Temporada.BAJA, tarifaBaja);
        this.tarifasPorTemporada.put(Temporada.MEDIA, tarifaMedia);
        this.tarifasPorTemporada.put(Temporada.ALTA, tarifaAlta);
        this.estado = EstadoApartamento.PREPARADO;
        this.activo = true;
    }

    public long getTarifa(Temporada temporada) {
        return tarifasPorTemporada.getOrDefault(temporada, 0L);
    }

    public void setFueraDeServicio(String motivo) {
        this.estado = EstadoApartamento.FUERA_SERVICIO;
        this.motivoFueraServicio = motivo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getNumeroDormitorios() { return numeroDormitorios; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public List<String> getCaracteristicas() { return caracteristicas; }
    public EstadoApartamento getEstado() { return estado; }
    public void setEstado(EstadoApartamento estado) { this.estado = estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Cap: " + capacidadMaxima + " | " + estado + ")";
    }
}