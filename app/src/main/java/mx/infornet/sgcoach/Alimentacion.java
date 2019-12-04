package mx.infornet.sgcoach;

public class Alimentacion {

    private int id;
    private String nombre;
    private String descripcion;
    private String categoria;

    public Alimentacion(int id, String nombre, String descripcion, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }
}
