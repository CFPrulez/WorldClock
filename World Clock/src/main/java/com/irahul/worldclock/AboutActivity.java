package com.irahul.worldclock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button author = (Button) findViewById(R.id.button_author);
        author.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(AboutActivity.this);
                View v = inflater.inflate(R.layout.scrollable_textview, null);

                TextView authorText = (TextView) v.findViewById(R.id.textview_scrollable);
                authorText.setText(R.string.author_text);
                authorText.setMovementMethod(LinkMovementMethod.getInstance());

                AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this).create();
                alertDialog.setTitle(R.string.author);
                alertDialog.setView(v);

                alertDialog.setButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });

        final Button license = (Button) findViewById(R.id.button_license);
        license.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(AboutActivity.this);
                View v = inflater.inflate(R.layout.scrollable_textview, null);

                TextView licenseText = (TextView) v.findViewById(R.id.textview_scrollable);
                licenseText.setText(R.string.gpl);
                licenseText.setMovementMethod(LinkMovementMethod.getInstance());

                AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this).create();
                alertDialog.setTitle(R.string.license);
                alertDialog.setView(v);

                alertDialog.setButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });

        final Button otherProjects = (Button) findViewById(R.id.button_other_projects);
        otherProjects.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(AboutActivity.this);
                View v = inflater.inflate(R.layout.scrollable_textview, null);

                TextView otherProjectsText = (TextView) v.findViewById(R.id.textview_scrollable);
                otherProjectsText.setText(R.string.included_projects_list);
                otherProjectsText.setMovementMethod(LinkMovementMethod.getInstance());

                AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this).create();
                alertDialog.setTitle(R.string.included_projects);
                alertDialog.setView(v);

                alertDialog.setButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}