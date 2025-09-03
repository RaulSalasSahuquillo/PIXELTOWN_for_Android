package com.example.paint;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;   // <-- IMPORTA ESTO
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // offsets para arrastrar
    private float dX, dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- UI ---
        DrawingView drawingView = findViewById(R.id.drawingView);
        Button btnUndo          = findViewById(R.id.btnUndo);
        Button btnRedo          = findViewById(R.id.btnRedo);
        Button btnClear         = findViewById(R.id.btnClear);
        Button btnConstruir     = findViewById(R.id.btnConstruir);
        Spinner spinner         = findViewById(R.id.spinnerColors);
        View root               = findViewById(R.id.main);
        View panelBotonera      = findViewById(R.id.panelBotonera);

        // --- acciones dibujo ---
        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());
        btnClear.setOnClickListener(v -> drawingView.clearCanvas());

        // --- spinner colores ---
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.color_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String name = parent.getItemAtPosition(pos).toString();
                int c;
                switch (name) {
                    case "Rojo":     c = Color.RED; break;
                    case "Azul":     c = Color.BLUE; break;
                    case "Verde":    c = Color.GREEN; break;
                    case "Amarillo": c = Color.YELLOW; break;
                    case "Magenta":  c = Color.MAGENTA; break;
                    default:         c = Color.BLACK; break;
                }
                drawingView.setStrokeColor(c);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // --- menú Construir ---
        btnConstruir.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenuInflater().inflate(R.menu.menu_construir, menu.getMenu());
            menu.setOnMenuItemClickListener((MenuItem item) -> {
                if (item.getItemId() == R.id.action_casa) {
                    // Crear una nueva casa dinámicamente
                    ImageView nuevaCasa = new ImageView(this);
                    nuevaCasa.setImageResource(R.drawable.casa);

                    // Tamaño en dp -> px (p.ej. 96dp)
                    int sizePx = dp(96);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
                    nuevaCasa.setLayoutParams(params);

                    // Añadir al root (FrameLayout)
                    ((FrameLayout) root).addView(nuevaCasa);

                    // Centrar por encima de la botonera cuando esté medido
                    root.post(() -> {
                        float cx = (root.getWidth() - nuevaCasa.getWidth()) / 2f;
                        float maxY = root.getHeight() - panelBotonera.getHeight() - nuevaCasa.getHeight();
                        float cy = Math.max(0, maxY / 2f);
                        nuevaCasa.setX(Math.max(0, cx));
                        nuevaCasa.setY(cy);
                    });

                    // Hacer arrastrable con límites
                    nuevaCasa.setOnTouchListener((viewTouch, event) -> {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                dX = viewTouch.getX() - event.getRawX();
                                dY = viewTouch.getY() - event.getRawY();
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                float newX = event.getRawX() + dX;
                                float newY = event.getRawY() + dY;
                                float maxX2 = root.getWidth() - viewTouch.getWidth();
                                float maxY2 = root.getHeight() - panelBotonera.getHeight() - viewTouch.getHeight();
                                viewTouch.setX(Math.max(0, Math.min(newX, maxX2)));
                                viewTouch.setY(Math.max(0, Math.min(newY, maxY2)));
                                return true;
                        }
                        return false;
                    });

                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    // helper: dp a px
    private int dp(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }
}

