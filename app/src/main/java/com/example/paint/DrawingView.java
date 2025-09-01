package com.example.paint;
// ↑ El "paquete" agrupa tus clases. Debe coincidir con la carpeta donde está el archivo
//   y con el package de tu MainActivity. Si difiere, cámbialo para que sea igual.

/*
 * Esta clase crea un "lienzo" donde el usuario puede pintar con el dedo.
 * Extiende (hereda de) android.view.View para poder dibujar en pantalla.
 * Conceptos clave:
 * - onDraw(Canvas): aquí se pinta en la pantalla.
 * - onTouchEvent(MotionEvent): aquí se leen los toques/gestos del usuario.
 * - Path: representa la línea que vas dibujando.
 * - Paint: configura el estilo de dibujo (color, grosor...).
 */

import android.content.Context;          // Proporciona información del entorno de la app.
import android.graphics.Bitmap;          // Para exportar lo dibujado a imagen (opcional).
import android.graphics.Canvas;          // Superficie donde "pintamos".
import android.graphics.Color;           // Colores predefinidos.
import android.graphics.Paint;           // Estilo del trazo: color, grosor, etc.
import android.graphics.Path;            // Trazo curvo o poligonal que vamos dibujando.
import android.util.AttributeSet;        // Atributos cuando la vista se crea desde XML.
import android.view.MotionEvent;         // Eventos táctiles (down, move, up).
import android.view.View;                // Clase base de todos los componentes visuales.

import java.util.ArrayList;              // Lista dinámica para guardar trazos.
import java.util.List;
import java.util.Stack;                  // Pila LIFO para "deshacer/rehacer".

public class DrawingView extends View {

    /*
     * Clase interna simple que agrupa:
     * - path: la forma del trazo (la línea que dibujaste).
     * - paint: cómo se ve el trazo (color, grosor...).
     * Por qué guardamos ambos: porque cada trazo puede haberse hecho con un color/grosor distinto.
     */
    private static class Stroke {
        Path path;     // Geometría del trazo.
        Paint paint;   // Estilo visual del trazo.
        Stroke(Path p, Paint paint) { this.path = p; this.paint = paint; }
    }

    // "strokes" contendrá todos los trazos definitivos (ya soltaste el dedo).
    private final List<Stroke> strokes = new ArrayList<>();

    // "undone" guardará los trazos que deshiciste, para poder rehacerlos luego.
    private final Stack<Stroke> undone = new Stack<>();

    // Estos representan lo que estás dibujando AHORA MISMO (mientras mueves el dedo).
    private Paint currentPaint; // Configuración del pincel actual (color, grosor...).
    private Path currentPath;   // El camino que estás trazando en este instante.

    // Variables para suavizar la línea: recordamos el último punto tocado.
    private float lastX, lastY;

    // Tolerancia de movimiento: si el dedo se mueve menos que esto, no añadimos puntos nuevos.
    private static final float TOUCH_TOLERANCE = 4f;

    // ==== CONSTRUCTORES ====
    // Se requieren tres variantes para que Android pueda crear esta vista desde código o XML.
    public DrawingView(Context context) {
        super(context);   // Llama al constructor de View.
        init();           // Configura valores por defecto.
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs); // Recibe atributos si se define en XML.
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // Método común de inicialización para todos los constructores.
    private void init() {
        // Creamos un "Paint" por defecto: negro y relativamente grueso.
        currentPaint = createNewPaint(Color.BLACK, 12f);

        // Ponemos el fondo del lienzo en blanco para que el dibujo resalte.
        setBackgroundColor(Color.WHITE);

        // Estas dos líneas ayudan a que la vista pueda recibir foco (no imprescindible aquí).
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    // Crea y devuelve un objeto Paint configurado.
    private Paint createNewPaint(int color, float strokeWidth) {
        Paint p = new Paint();
        p.setAntiAlias(true);                   // Suaviza los bordes del trazo.
        p.setDither(true);                      // Mejora gradientes/colores en pantallas.
        p.setColor(color);                      // Color del trazo.
        p.setStyle(Paint.Style.STROKE);         // "STROKE" = solo contorno (no relleno).
        p.setStrokeJoin(Paint.Join.ROUND);      // Uniones redondeadas entre segmentos.
        p.setStrokeCap(Paint.Cap.ROUND);        // Extremos del trazo redondeados.
        p.setStrokeWidth(strokeWidth);          // Grosor del trazo en píxeles.
        return p;
    }

    // onDraw se llama automáticamente cuando Android necesita que la vista se repinte.
    // Recibe un "Canvas" donde dibujamos todo.
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1) Dibujamos todos los trazos finalizados (guardados en "strokes").
        for (Stroke s : strokes) {
            canvas.drawPath(s.path, s.paint);
        }

        // 2) Si hay un trazo en curso (mientras mueves el dedo), también lo mostramos.
        if (currentPath != null) {
            canvas.drawPath(currentPath, currentPaint);
        }
    }

    // onTouchEvent recibe los eventos táctiles: pulsar (DOWN), mover (MOVE), soltar (UP).
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Coordenadas del punto donde tocaste o moviste el dedo.
        float x = event.getX();
        float y = event.getY();

        // switch para separar el comportamiento según el tipo de evento.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // El dedo toca la pantalla por primera vez.
                startTouch(x, y);         // Empezamos un nuevo Path.
                invalidate();             // Pide a Android que llame a onDraw() de nuevo.
                return true;              // true = consumimos el evento (nos hacemos cargo).

            case MotionEvent.ACTION_MOVE: // El dedo se desplaza por la pantalla.
                moveTouch(x, y);          // Actualizamos el Path con puntos intermedios suavizados.
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:   // El dedo se levanta (fin del trazo actual).
                upTouch();                // Guardamos el trazo en la lista "strokes".
                invalidate();
                return true;
        }

        // Si no era un evento que nos interese, delegamos al comportamiento por defecto.
        return super.onTouchEvent(event);
    }

    // Comienza un Path nuevo en (x, y).
    private void startTouch(float x, float y) {
        currentPath = new Path(); // Creamos un camino vacío.
        currentPath.moveTo(x, y); // Posicionamos el "lápiz" en (x, y) sin dibujar aún.
        lastX = x;                // Guardamos la posición para suavizados futuros.
        lastY = y;
        undone.clear();           // Si empiezas un nuevo trazo, limpiar la pila de "rehacer".
    }

    // Continúa el Path mientras el dedo se mueve.
    private void moveTouch(float x, float y) {
        // Calculamos cuánto nos movimos respecto al último punto.
        float dx = Math.abs(x - lastX);
        float dy = Math.abs(y - lastY);

        // Si el movimiento es suficientemente grande, añadimos un segmento curvo:
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // quadTo crea una curva suave usando el punto anterior y el punto medio.
            // (x + lastX)/2 y (y + lastY)/2 generan ese punto medio para suavizar la línea.
            currentPath.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);

            // Actualizamos "último punto" para la siguiente iteración.
            lastX = x;
            lastY = y;
        }
    }

    // Termina el trazo actual y lo añade a la lista definitiva.
    private void upTouch() {
        if (currentPath != null) {
            // Creamos una "foto" del Paint actual para que el trazo conserve su estilo.
            Paint snapshot = new Paint(currentPaint);

            // Añadimos el trazo terminado a la lista de trazos definitivos.
            strokes.add(new Stroke(currentPath, snapshot));

            // Ya no hay trazo actual en curso.
            currentPath = null;
        }
    }

    // ===== Métodos públicos para controlar la vista desde la Activity =====

    // Cambia el color del pincel (afecta a los trazos que se dibujen a partir de ahora).
    public void setStrokeColor(int color) {
        currentPaint.setColor(color);
    }

    // Cambia el grosor del pincel en píxeles.
    public void setStrokeWidth(float w) {
        currentPaint.setStrokeWidth(w);
    }

    // Deshace el último trazo hecho (si hay alguno).
    public void undo() {
        if (!strokes.isEmpty()) {
            // remove(...) saca el último trazo de la lista y lo empuja a la pila "undone".
            undone.push(strokes.remove(strokes.size() - 1));
            invalidate(); // Vuelve a dibujar sin ese trazo.
        }
    }

    // Rehace el último trazo que deshiciste (si hay alguno).
    public void redo() {
        if (!undone.isEmpty()) {
            // pop() saca el elemento de la pila y lo reañade a la lista de trazos.
            strokes.add(undone.pop());
            invalidate();
        }
    }

    // Limpia por completo el lienzo (borra todo).
    public void clearCanvas() {
        strokes.clear(); // Vacía la lista de trazos.
        undone.clear();  // Vacía también lo que se podía rehacer.
        invalidate();    // Redibuja la vista (ahora en blanco).
    }

    // Crea una imagen (Bitmap) con lo dibujado, por si luego quieres guardarlo como PNG.
    public Bitmap exportBitmap(int backgroundColor) {
        // Creamos un Bitmap del tamaño actual de la vista.
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        // Asociamos un Canvas a ese Bitmap para poder "dibujarle" encima.
        Canvas c = new Canvas(b);

        // Pintamos un fondo (por ejemplo, blanco).
        c.drawColor(backgroundColor);

        // Dibujamos todos los trazos definitivos sobre ese bitmap.
        for (Stroke s : strokes) c.drawPath(s.path, s.paint);

        // Devolvemos el resultado para que otra clase lo guarde en archivo si quiere.
        return b;
    }
}
