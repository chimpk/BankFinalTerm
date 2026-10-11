package vn.edu.tdtu.tdtbank.ui.officer;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.ui.common.BaseShellActivity;

public class OfficerMainActivity extends BaseShellActivity {

    @Override
    protected int navGraphRes() {
        return R.navigation.nav_officer;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_officer;
    }

    @Override
    protected int toolbarMenuRes() {
        return R.menu.menu_toolbar_officer;
    }

    @Override
    protected int[] topLevelDestinations() {
        return new int[]{R.id.customerListFragment, R.id.kycQueueFragment, R.id.ratesFragment, R.id.profileFragment};
    }

    @Override
    protected Role requiredRole() {
        return Role.OFFICER;
    }
}
