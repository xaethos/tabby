package net.xaethos.tabby;

import net.xaethos.tabby.fragment.RepresentationFragment;
import net.xaethos.tabby.fragment.RepresentationFragment.OnLinkFollowListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.theoryinpractise.halbuilder.api.Link;

public class MainActivity extends FragmentActivity implements OnLinkFollowListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(android.R.id.content) == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment fragment = RepresentationFragment.withURI("/");
            transaction.add(android.R.id.content, fragment);
            transaction.commit();
        }
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.activity_main, menu);
    // return true;
    // }

    // *** OnLinkFollowListener implementation

    @Override
    public void onFollowLink(Link link) {
        if (link == null) {
            Toast.makeText(this, "No link to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (link.hasTemplate()) {
            Toast.makeText(this, "Can't follow templated links", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = RepresentationFragment.withURI(link.getHref());
        transaction.replace(android.R.id.content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
