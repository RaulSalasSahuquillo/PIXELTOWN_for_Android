package com.example.paint; // <- El "paquete" agrupa archivos Java. Debe coincidir con tu app.

/*
 * Importamos clases de Android que vamos a usar.
 * Piensa en "import" como traer herramientas a tu archivo.
 * He dejado solo las realmente utilizadas para mantener el archivo limpio.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.content.DialogInterface;


import android.view.GestureDetector;        // Detecta gestos "un dedo" (scroll, doble-tap, etc.)
import android.view.Menu;                  // Para inflar el menú en la toolbar
import android.view.MenuItem;              // Representa un elemento pulsado en un menú.
import android.view.MotionEvent;           // Eventos de toque (down/move/up) para arrastrar.
import android.view.ScaleGestureDetector;  // Detecta gesto de pinza (zoom)
import android.view.View;                  // Clase base para todos los elementos de UI.

import android.widget.FrameLayout;         // Contenedor que apila vistas (como “capas”).
import android.widget.ImageView;           // Para mostrar imágenes en pantalla.
import android.widget.PopupMenu;           // Menú contextual emergente anclado a un botón o toolbar.
import android.widget.Toast;               // Mensajes breves en pantalla (notificaciones tipo “toast”).

import androidx.activity.EdgeToEdge;       // Permite que la UI ocupe toda la pantalla.
import androidx.appcompat.app.AlertDialog; // Cuadros de diálogo nativos.
import androidx.appcompat.app.AppCompatActivity; // Clase base de una pantalla (Activity).
import androidx.appcompat.widget.Toolbar;  // Barra superior (app bar)
import androidx.core.graphics.Insets;      // Tamaños de las barras del sistema (status/nav).
import androidx.core.view.ViewCompat;      // Utilidades para trabajar con vistas de forma moderna.
import androidx.core.view.WindowInsetsCompat; // Acceso unificado a los "insets" del sistema.

import android.content.SharedPreferences;  // Para guardar/cargar datos simples (autoguardado).

import org.json.JSONArray;                 // Serializar lista -> JSON
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;                // Lista en memoria de edificios

/*
 * Una Activity es una PANTALLA de tu app.
 * MainActivity es la pantalla principal que se lanza al abrir la app.
 */
public class MainActivity extends AppCompatActivity {

    // ===== Persistencia y estado global =====
    private SharedPreferences prefs;
    private DrawingView drawingView; // para cambiar color/grosor desde el menú

    // --- Estado "tipo juego" (tu lógica actual) ---
    private int money = 10000;   // saldo inicial
    private int experience = 0;  // puntos de experiencia
    private int buildingsCount = 0;
    private int happiness = 50;
    private int ronda = 1;
    private int tarraco = 0;
    private int poblation = 3;
    private int casas = 0;
    private int ayuntamiento = 0;
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

    private void showImpuestos() {
        if (ronda == 1 || ronda == 2 || ronda == 3 || ronda == 4) {
            String msg = "Los impuestos iniciales son del 15%.";
            new AlertDialog.Builder(this).setTitle(" ").setMessage(msg)
                    .setPositiveButton("CERRAR", null).show();
        } else if (ronda == 5) {
            String msg = "Los impuestos iniciales son del 15%. Vamos a subirlos solo un poco para poder construir más.";
            new AlertDialog.Builder(this)
                    .setTitle(" ")
                    .setMessage(msg)
                    .setPositiveButton("AUMENTAR IMPUESTOS UN 5%", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 👇 Aquí escribes la acción que quieres que haga
                            porcentajeimpuestos += 5; // por ejemplo, subir variable impuestos
                            impuestos ++;
                            Toast.makeText(MainActivity.this, "Impuestos aumentados al " + porcentajeimpuestos + "%", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
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

    // ===== Zoom / Pan (NUEVO) =====
    /*
     * La idea: todo lo que deba hacer zoom/arrastrarse está dentro de un contenedor
     * con id @id/world. Le aplicamos transformaciones (escala + traslación) con gestos.
     */
    private ScaleGestureDetector scaleDetector; // pinch-to-zoom
    private GestureDetector gestureDetector;    // pan (arrastre) y doble-tap
    private float scale = 1f;                   // escala actual (1 = 100%)
    private static final float MIN_SCALE = 0.5f, MAX_SCALE = 3f; // límites de zoom
    private float panX = 0f, panY = 0f;         // desplazamiento actual

    private View world;      // contenedor que se escala y mueve
    private ImageView bg;    // fondo del mapa

    // Para limitar el pan según tamaño de contenido y de la pantalla
    private int viewportW = 0, viewportH = 0;   // tamaño visible (pantalla)
    private int contentW = 0, contentH = 0;     // tamaño "real" del contenido (bitmap fondo)

    // ===== Ciclo de vida =====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- Referencias principales ---
        bg = findViewById(R.id.bg);
        world = findViewById(R.id.world);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Cargar fondo de forma segura/escalada ---
        // Tamaño de pantalla aproximado para escalar
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int reqW = dm.widthPixels;
        int reqH = dm.heightPixels;

        BitmapFactory.Options o = new BitmapFactory.Options();
        // 1) Sólo medir
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.fondoo, o);

        // 2) Calcular inSampleSize (potencia de 2) para no cargar más de lo necesario
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

        // --- Dimensiones del contenido (para límites de pan) ---
        if (bg.getDrawable() != null) {
            contentW = bg.getDrawable().getIntrinsicWidth();
            contentH = bg.getDrawable().getIntrinsicHeight();
        }

        // --- Insets para edge-to-edge (sobre el root principal) ---
        View mainRoot = findViewById(R.id.main);
        View panelBotonera = findViewById(R.id.panelBotonera);
        drawingView = findViewById(R.id.drawingView);

        ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Preferencias y carga de estado (antes de restaurar posiciones) ---
        prefs = getSharedPreferences("pixeltown_save", MODE_PRIVATE);
        loadGame();

        // --- Medir viewport cuando el árbol esté listo ---
        world.post(() -> {
            viewportW = world.getWidth();
            viewportH = world.getHeight();
            applyTransform(); // estado inicial (escala/posición)
            // Restaurar edificios después de medir y de tener estado cargado
            restoreBuildings(world, panelBotonera);
        });

        // --- Detectores de gestos (pinch y pan) ---
        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());

        // --- Tocar el "mundo" = pan/zoom ---
        world.setOnTouchListener((v, e) -> {
            boolean s = scaleDetector.onTouchEvent(e); // pinch
            boolean g = gestureDetector.onTouchEvent(e); // pan & double-tap
            if (e.getActionMasked() == MotionEvent.ACTION_UP ||
                    e.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                // Al soltar, limitamos pan y aplicamos transform
                clampPan();
                applyTransform();
            }
            return s || g; // consumimos si hubo gesto
        });

        // (Opcional) Oculta la botonera si usas solo la Toolbar
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

        // OJO: ahora "root" debe ser el contenedor que hace zoom (world)
        View worldRoot = findViewById(R.id.world);
        View panelBotonera = findViewById(R.id.panelBotonera);
        Toolbar tb = findViewById(R.id.toolbar);

        if (id == R.id.action_construir) {
            // Reutilizamos tu menú de construir, anclado a la Toolbar
            mostrarMenuConstruir(tb, worldRoot, panelBotonera);
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
        } else if (id == R.id.action_impuestos) {
            showImpuestos();
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
        else if (ronda == 2 && ayuntamiento == 1) ok = true;
        else if (ronda == 3 && farolas == 4 && seguridad == 5) ok = true;
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
                    crearEdificio(root, panelBotonera, R.drawable.casa, 48);
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
                    crearEdificio(root, panelBotonera, R.drawable.tarraco, 60);
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
                if (money >= 10000) {
                    money -= 10000;
                    experience += 100;
                    happiness += 10;
                    ayuntamiento = 1;
                    crearEdificio(root, panelBotonera, R.drawable.ayuntamiento, 60);
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
                    crearEdificio(root, panelBotonera, R.drawable.primaprix, 60);
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
                    crearEdificio(root, panelBotonera, R.drawable.esclat, 60);
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
                    crearEdificio(root, panelBotonera, R.drawable.esclat, 60); // cambia drawable si tienes marvimundo
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
                    crearEdificio(root, panelBotonera, R.drawable.estacionpolicial, 60);
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
                    crearEdificio(root, panelBotonera, R.drawable.camaradeseguridad, 25);
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
                    crearEdificio(root, panelBotonera, R.drawable.policiaestable, 25);
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
                    farolas ++;
                    crearEdificio(root, panelBotonera, R.drawable.farola, 60);
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

        // Centramos el edificio recién creado y guardamos
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

    // ===== Gestos y transformaciones (zoom/pan) =====
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override public boolean onScale(ScaleGestureDetector d) {
            float prev = scale;
            float next = Math.max(MIN_SCALE, Math.min(scale * d.getScaleFactor(), MAX_SCALE));

            // Mantener el punto de enfoque "fijo" al escalar (zoom centrado en los dedos)
            float fx = d.getFocusX(), fy = d.getFocusY();
            panX = fx - (fx - panX) * (next / prev);
            panY = fy - (fy - panY) * (next / prev);

            scale = next;
            clampPan();
            applyTransform();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override public boolean onDown(MotionEvent e) { return true; } // imprescindible para que onScroll dispare
        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            // dx/dy = desplazamiento desde el último evento → pan en sentido contrario
            panX -= dx;
            panY -= dy;
            clampPan();
            applyTransform();
            return true;
        }
        @Override public boolean onDoubleTap(MotionEvent e) {
            // Reset rápido al centro (escala 1x)
            scale = 1f; panX = 0f; panY = 0f;
            applyTransform();
            return true;
        }
    }

    private void applyTransform() {
        // Aplica escala y traslación al contenedor "world"
        if (world == null) return;
        world.setScaleX(scale);
        world.setScaleY(scale);
        world.setTranslationX(panX);
        world.setTranslationY(panY);
    }

    private void clampPan() {
        // Limita el pan para que no aparezcan bordes en blanco (si el contenido es >= viewport)
        if (viewportW == 0 || viewportH == 0 || contentW == 0 || contentH == 0) return;

        // tamaño del mundo escalado
        float worldW = contentW * scale;
        float worldH = contentH * scale;

        // límites: permitimos como máximo posicionar el mundo de 0 a (viewport - mundo)
        float minX = Math.min(0f, viewportW - worldW);
        float minY = Math.min(0f, viewportH - worldH);
        float maxX = 0f, maxY = 0f;

        panX = Math.max(minX, Math.min(panX, maxX));
        panY = Math.max(minY, Math.min(panY, maxY));
    }
}
