package com.example.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.reddit.utilities.PreferencesManager;

/**
 * TODO Initialiser les settings avec ceux sauvegarder
 */
public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initialiserToolbar();

        initialiserSpinner(R.id.setting_affichage_spinner, Settings.Interface, Settings.Interface_key);
        initialiserSpinner(R.id.setting_nsfw_spinner, Settings.NSFW, Settings.NSFW_key);
    }

    /**
     * Initialise la toolbar. Elle est ajout�e et attach�e au layout
     */
    private void initialiserToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolbar);
    }

    /**
     * Permet d'initialiser un spinner avec une list de string
     *
     * @param id
     * @param entries
     */
    private void initialiserSpinner(int id, String[] entries, String key) {

        // Init
        Spinner spinner = (Spinner) findViewById(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, entries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Met le spinner a la bonne position, si il y a deja un setting d'enregistre
        PreferencesManager pm = PreferencesManager.getInstance();
        String value = pm.getPreference(key).toString();
        for (int i = 0; i < entries.length; i++)
            if (value.equals(entries[i]))
                spinner.setSelection(i);
    }

    /**
     * Permet de sauvegarder les settings
     * @param view
     */
    public void onClickSave(View view) {

        PreferencesManager pm = PreferencesManager.getInstance();

        pm.setPreference(Settings.Interface_key,
                ((Spinner) findViewById(R.id.setting_affichage_spinner)).getSelectedItem().toString());

        pm.setPreference(Settings.NSFW_key,
                ((Spinner) findViewById(R.id.setting_nsfw_spinner)).getSelectedItem().toString());

        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
