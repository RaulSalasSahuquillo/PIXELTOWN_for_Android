package com.example.paint; // <- El "paquete" agrupa archivos Java. Debe coincidir con tu app.

/*
 * Importamos clases de Android que vamos a usar.
 * Piensa en "import" como traer herramientas a tu archivo.
 */
import android.graphics.Color;              // Colores básicos (BLACK, RED, etc).
import android.os.Bundle;                  // Objeto con datos del estado al crear la Activity.
import android.util.TypedValue;            // Para convertir dp -> px de forma correcta.
import android.view.MenuItem;              // Representa un elemento pulsado en un menú.
import android.view.MotionEvent;           // Eventos de toque (down/move/up) para arrastrar.
import android.view.View;                  // Clase base para todos los elementos de UI.
import android.widget.ArrayAdapter;        // Adaptador sencillo para listas/Spinner. (si mantienes spinner)
import android.widget.Button;              // Botón normal. (si mantienes botonera)
import android.widget.FrameLayout;         // Contenedor que apila vistas (como “capas”).
import android.widget.ImageView;           // Para mostrar imágenes en pantalla.
import android.widget.PopupMenu;           // Menú contextual emergente anclado a un botón o toolbar.
import android.widget.Spinner;             // Desplegable para elegir una opción (colores). (si mantienes spinner)
import android.widget.Toast;               // Mensajes breves en pantalla (notificaciones tipo “toast”).
import androidx.appcompat.widget.Toolbar;  // Barra superior (app bar)
import android.view.Menu;                  // Para inflar el menú en la toolbar
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.widget.ImageView;

// Si el código va en MainActivity:
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;  // Para guardar/cargar datos simples (autoguardado).

import androidx.activity.EdgeToEdge;       // Permite que la UI ocupe toda la pantalla.
import androidx.appcompat.app.AlertDialog; // Cuadros de diálogo nativos.
import androidx.appcompat.app.AppCompatActivity; // Clase base de una pantalla (Activity).
import androidx.core.graphics.Insets;      // Tamaños de las barras del sistema (status/nav).
import androidx.core.view.ViewCompat;      // Utilidades para trabajar con vistas de forma moderna.
import androidx.core.view.WindowInsetsCompat; // Acceso unificado a los "insets" del sistema.

import java.util.ArrayList;                // Lista en memoria de edificios
import org.json.JSONArray;                 // Serializar lista -> JSON
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Una Activity es una PANTALLA de tu app.
 * MainActivity es la pantalla principal que se lanza al abrir la app.
 */
public class MainActivity extends AppCompatActivity {

    // ===== Persistencia y estado global =====
    private SharedPreferences prefs;
    private DrawingView drawingView; // para cambiar color/grosor desde el menú

    private int money = 10000;   // saldo inicial
    private int experience = 0;  // puntos de experiencia
    private int buildingsCount = 0;
    private int happiness = 50;
    private int ronda = 1;
    private int tarraco = 0;
    private int poblation = 3;
    private int casas = 0;
    private int ayuntamiento = 1;
    private int farolas = 0;
    private int seguridad = 0;
    private int estacionpolicial = 0;
    private int policia = 0;
    private int impuestos = 0;
    private int tienda = 0;
    private int porcentajeimpuestos = 15;
    private int dinero1 = 1000;
    private int dinero2 = 34000;
    private int dinerototal = 0;


    // Variables para recordar el desplazamiento al arrastrar edificios
    private float dX, dY;

    // ---- Estado de edificios ----
    private static class Building {
        int drawableId;
        int sizeDp;
        float x, y;

        Building(int drawableId, int sizeDp, float x, float y) {
            this.drawableId = drawableId; this.sizeDp = sizeDp; this.x = x; this.y = y;
        }
        JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("drawableId", drawableId);
            o.put("sizeDp", sizeDp);
            o.put("x", x);
            o.put("y", y);
            return o;
        }
        static Building fromJson(JSONObject o) throws JSONException {
            return new Building(
                    o.getInt("drawableId"),
                    o.getInt("sizeDp"),
                    (float) o.getDouble("x"),
                    (float) o.getDouble("y")
            );
        }
    }
    private final ArrayList<Building> buildingState = new ArrayList<>();

    // ====== Diálogos del juego ======
    private void showStatsDialog() {
        String msg = "Dinero: " + money + " Հ"
                + "\nExperiencia: " + experience
                + "\nEdificios: " + buildingsCount
                + "\nFelicidad: " + happiness + " %"
                + "\nRonda: " + ronda
                + "\nPoblación: " + poblation
                + "\nImpuestos: " + porcentajeimpuestos + " %";
        new AlertDialog.Builder(this)
                .setTitle("Estadísticas")
                .setMessage(msg)
                .setPositiveButton("SALIR", null)
                .show();
    }

    private void showMission() {
        if (ronda == 1){
            String msg = "Hay 3 habitantes (incluyéndote a tí). ¿Qué te parece si empezamos construyendo 3 casas?";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        }
        if (ronda == 2 && ayuntamiento == 0) {
            String msg = "¿Pero cómo vas a gobernar? ¿Qué te parece si construimos un ayuntamiento?";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        } else if ( ronda == 2 && ayuntamiento == 1) {
            String msg = "Haz tu primer paso como gobernador. Construye una tienda";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        }
        if (ronda == 3 && farolas == 0 && seguridad == 0) {
            String msg = "La gente se está quejando de que no ve por la noche. Construye 4 farolas alrededor la la ciudad";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        } else if (ronda == 3 && farolas == 4 && seguridad == 0){
            String msg = "Pon 5 cámaras de seguridad: 4 en farolas y 1 en el ayuntamiento.";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        }
        if (ronda == 4 && estacionpolicial == 0) {
            String msg = "Pon una estación de policía";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        } else if (ronda == 4 && estacionpolicial == 1 && policia == 0) {
            String msg = "Contrata a un policía";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        } else if (ronda == 4 && estacionpolicial == 1 && policia == 1 && casas == 3) {
            String msg = "Construye una casa para el policía.";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        }
        if (ronda == 5 && impuestos == 0) {
            String msg = "¡Sube los impuestos para ganar más dinero!";
            new AlertDialog.Builder(this).setTitle("Misiones").setMessage(msg)
                    .setPositiveButton("MANOS A LA OBRA", null).show();
        }
    }

    // ===== Ciclo de vida =====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView bg = findViewById(R.id.bg);

// Tamaño de pantalla aproximado para escalar
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int reqW = dm.widthPixels;
        int reqH = dm.heightPixels;

        BitmapFactory.Options o = new BitmapFactory.Options();
// 1) Sólo medir
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.fondoo, o);

// 2) Calcular inSampleSize (potencia de 2)
        int inSample = 1;
        int halfH = o.outHeight / 2;
        int halfW = o.outWidth / 2;
        while ((halfH / inSample) >= reqH && (halfW / inSample) >= reqW) {
            inSample *= 2;
        }

// 3) Decodificar ya escalado y en formato que gasta menos RAM
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inJustDecodeBounds = false;
        o2.inSampleSize = inSample;
        o2.inPreferredConfig = Bitmap.Config.RGB_565; // 50% menos memoria que ARGB_8888
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.fondoo, o2);

// 4) Asignar
        bg.setImageBitmap(bmp);


        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Preferencias y carga de estado
        prefs = getSharedPreferences("pixeltown_save", MODE_PRIVATE);
        loadGame();

        // Referencias de vistas
        View root = findViewById(R.id.main);
        View panelBotonera = findViewById(R.id.panelBotonera);
        drawingView = findViewById(R.id.drawingView);

        // Insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Restaurar edificios después de loadGame y de tener referencias
        root.post(() -> restoreBuildings(root, panelBotonera));

        // Ocultar la botonera si ya usas la Toolbar (puedes quitar esta línea si quieres mostrarla)
        if (panelBotonera != null) panelBotonera.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGame();
    }

    // ===== Menú de la Toolbar =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        View root = findViewById(R.id.main);
        View panelBotonera = findViewById(R.id.panelBotonera);
        Toolbar tb = findViewById(R.id.toolbar);

        if (id == R.id.action_construir) {
            // Reutilizamos tu menú de construir, anclado a la Toolbar
            mostrarMenuConstruir(tb, root, panelBotonera);
            return true;

        } else if (id == R.id.action_color) {
            mostrarSelectorColor();
            return true;

        } else if (id == R.id.action_ronda) {
            intentarCambiarRonda();
            return true;

        } else if (id == R.id.action_mision) {
            showMission();
            return true;

        } else if (id == R.id.action_stats) {
            showStatsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ===== Lógica de acciones del menú =====
    private void mostrarSelectorColor() {
        final String[] nombres = {"Negro", "Rojo", "Azul", "Verde", "Amarillo", "Magenta"};
        final int[] valores = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA };

        new AlertDialog.Builder(this)
                .setTitle("Color del pincel")
                .setItems(nombres, (d, which) -> {
                    if (which >= 0 && which < valores.length && drawingView != null) {
                        drawingView.setStrokeColor(valores[which]);
                    }
                })
                .show();
    }

    private void intentarCambiarRonda() {
        boolean ok = false;
        if (ronda == 1 && casas == 3) ok = true;
        else if (ronda == 2 && ayuntamiento == 1 && tienda == 1) ok = true;
        else if (ronda == 3 && farolas == 4 && seguridad == 4) ok = true;
        else if (ronda == 4 && estacionpolicial == 1 && policia == 1 && casas == 4) ok = true;

        if (!ok) {
            Toast.makeText(this, "¡Debes completar las misiones antes de cambiar de ronda!", Toast.LENGTH_SHORT).show();
            return;
        }

        ronda++;
        int total = dinero1 + dinero2;
        money += total * porcentajeimpuestos / 100;
        happiness--;
        Toast.makeText(this, "¡Ronda " + ronda + "!", Toast.LENGTH_SHORT).show();
        saveGame();
    }

    private void mostrarMenuConstruir(View anchor, View root, View panelBotonera) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenuInflater().inflate(R.menu.menu_construir, menu.getMenu());

        menu.setOnMenuItemClickListener((MenuItem item) -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_casa) {
                if (money >= 1000) {
                    money -= 1000;
                    experience += 10;
                    happiness++;
                    casas++;
                    crearEdificio(root, panelBotonera, R.drawable.casa, 96);
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_tarraco) {
                if (money >= 50000 && tarraco == 0) {
                    money -= 50000;
                    experience += 500;
                    happiness += 10;
                    crearEdificio(root, panelBotonera, R.drawable.tarraco, 120);
                    tarraco++;
                    Toast.makeText(this, "¡Has construido tu primera distribuidora!", Toast.LENGTH_SHORT).show();
                    saveGame();
                } else if (tarraco == 1) {
                    Toast.makeText(this, "¡Ya tienes un Tarraco!", Toast.LENGTH_SHORT).show();
                } else if (money < 50000) {
                    Toast.makeText(this, "¡No tienes suficiente dinero!", Toast.LENGTH_SHORT).show();
                    happiness -= 5;
                }
                return true;

            } else if (itemId == R.id.action_ayuntamiento) {
                if (money >= 20000) {
                    money -= 20000;
                    experience += 100;
                    happiness += 10;
                    ayuntamiento = 1;
                    crearEdificio(root, panelBotonera, R.drawable.ayuntamiento, 120);
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness -= 10;
                }
                return true;

            } else if (itemId == R.id.action_primaprix) {
                if (money >= 5000) {
                    money -= 5000;
                    experience += 30;
                    happiness++;
                    tienda++;
                    crearEdificio(root, panelBotonera, R.drawable.primaprix, 120);
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_esclat) {
                if (money >= 6000) {
                    money -= 6000;
                    experience += 30;
                    happiness++;
                    tienda++;
                    crearEdificio(root, panelBotonera, R.drawable.esclat, 120);
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_marvimundo) {
                if (money >= 7000) {
                    money -= 7000;
                    experience += 30;
                    tienda++;
                    crearEdificio(root, panelBotonera, R.drawable.esclat, 120); // cambia drawable si tienes marvimundo
                    happiness++;
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_estacion) {
                if (money >= 8000) {
                    money -= 8000;
                    experience += 35;
                    estacionpolicial = 1;
                    crearEdificio(root, panelBotonera, R.drawable.estacionpolicial, 120);
                    happiness++;
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_camara) {
                if (money >= 50) {
                    money -= 50;
                    experience += 5;
                    seguridad++; // opcional si así llevas la cuenta
                    crearEdificio(root, panelBotonera, R.drawable.camaradeseguridad, 50);
                    happiness++;
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_policia) {
                if (money >= 2500) {
                    money -= 2500;
                    experience += 400;
                    policia++; // opcional si así llevas la cuenta
                    crearEdificio(root, panelBotonera, R.drawable.policiaestable, 50);
                    happiness++;
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;

            } else if (itemId == R.id.action_farola) {
                if (money >= 100) {
                    money -= 100;
                    experience += 6;
                    farolas = 1;
                    crearEdificio(root, panelBotonera, R.drawable.farola, 120);
                    happiness++;
                    saveGame();
                } else {
                    Toast.makeText(this, "No tienes suficiente dinero", Toast.LENGTH_SHORT).show();
                    happiness--;
                }
                return true;
            }
            return false;
        });

        menu.show();
    }

    // ===== Persistencia =====
    private void saveGame() {
        if (prefs == null) return;
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("money", money);
        e.putInt("experience", experience);
        e.putInt("buildingsCount", buildingsCount);
        e.putInt("happiness", happiness);
        e.putInt("ronda", ronda);
        e.putInt("tarraco", tarraco);
        e.putInt("poblation", poblation);
        e.putInt("casas", casas);
        e.putInt("ayuntamiento", ayuntamiento);
        e.putInt("farolas", farolas);
        e.putInt("seguridad", seguridad);
        e.putInt("estacionpolicial", estacionpolicial);
        e.putInt("policia", policia);
        e.putInt("impuestos", impuestos);
        e.putInt("tienda", tienda);
        e.putInt("porcentajeimpuestos", porcentajeimpuestos);
        e.putInt("dinero1", dinero1);
        e.putInt("dinero2", dinero2);
        e.putInt("dinerototal", dinerototal);

        // Guarda edificios como JSON
        JSONArray arr = new JSONArray();
        try { for (Building b : buildingState) arr.put(b.toJson()); } catch (JSONException ignore) {}
        e.putString("buildings_json", arr.toString());

        e.apply();
    }

    private void loadGame() {
        if (prefs == null) return;
        money            = prefs.getInt("money", money);
        experience       = prefs.getInt("experience", experience);
        buildingsCount   = prefs.getInt("buildingsCount", buildingsCount);
        happiness        = prefs.getInt("happiness", happiness);
        ronda            = prefs.getInt("ronda", ronda);
        tarraco          = prefs.getInt("tarraco", tarraco);
        poblation        = prefs.getInt("poblation", poblation);
        casas            = prefs.getInt("casas", casas);
        ayuntamiento     = prefs.getInt("ayuntamiento", ayuntamiento);
        farolas          = prefs.getInt("farolas", farolas);
        seguridad        = prefs.getInt("seguridad", seguridad);
        estacionpolicial = prefs.getInt("estacionpolicial", estacionpolicial);
        policia          = prefs.getInt("policia", policia);
        impuestos        = prefs.getInt("impuestos", impuestos);
        tienda           = prefs.getInt("tienda", tienda);
        porcentajeimpuestos = prefs.getInt("porcentajeimpuestos", porcentajeimpuestos);
        dinero1          = prefs.getInt("dinero1", dinero1);
        dinero2          = prefs.getInt("dinero2", dinero2);
        dinerototal      = prefs.getInt("dinerototal", dinerototal);

        // Carga edificios como JSON
        buildingState.clear();
        String raw = prefs.getString("buildings_json", "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                buildingState.add(Building.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException ignore) {}
    }

    // ===== Edificios (creación, restauración y drag) =====
    private void crearEdificio(View root, View panelBotonera, int drawableId, int sizeDp) {
        ImageView edificio = new ImageView(this);
        edificio.setImageResource(drawableId);

        int sizePx = dp(sizeDp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        edificio.setLayoutParams(params);

        ((FrameLayout) root).addView(edificio);
        buildingsCount++;

        // Crear estado y asociarlo
        Building b = new Building(drawableId, sizeDp, 0f, 0f);
        edificio.setTag(b);
        buildingState.add(b);

        root.post(() -> {
            float cx = (root.getWidth() - edificio.getWidth()) / 2f;
            float maxY = root.getHeight() - (panelBotonera != null ? panelBotonera.getHeight() : 0) - edificio.getHeight();
            float cy = Math.max(0, maxY / 2f);

            edificio.setX(Math.max(0, cx));
            edificio.setY(cy);

            // Guardar posición en el estado
            b.x = edificio.getX();
            b.y = edificio.getY();

            saveGame(); // guardado inmediato
        });

        attachDragBehavior(edificio, root, panelBotonera);
    }

    private void restoreBuildings(View root, View panelBotonera) {
        for (Building b : buildingState) {
            crearEdificioDesdeEstado(root, panelBotonera, b);
        }
    }

    private void crearEdificioDesdeEstado(View root, View panelBotonera, Building b) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(b.drawableId);
        int sizePx = dp(b.sizeDp);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        iv.setLayoutParams(params);
        ((FrameLayout) root).addView(iv);
        buildingsCount++;

        iv.setX(b.x);
        iv.setY(b.y);

        iv.setTag(b);
        attachDragBehavior(iv, root, panelBotonera);
    }

    private void attachDragBehavior(ImageView viewTouch, View root, View panelBotonera) {
        viewTouch.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    float maxX = root.getWidth() - v.getWidth();
                    float maxY2 = root.getHeight() - (panelBotonera != null ? panelBotonera.getHeight() : 0) - v.getHeight();

                    v.setX(Math.max(0, Math.min(newX, maxX)));
                    v.setY(Math.max(0, Math.min(newY, maxY2)));

                    // Actualiza posición en el estado
                    Object tag = v.getTag();
                    if (tag instanceof Building) {
                        Building b = (Building) tag;
                        b.x = v.getX();
                        b.y = v.getY();
                    }
                    return true;
            }
            return false;
        });
    }

    // ===== Utilidades =====
    private int dp(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        ));
    }
}



