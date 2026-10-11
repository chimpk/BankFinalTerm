package vn.edu.tdtu.tdtbank.ui.customer;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.ui.common.BaseShellActivity;

public class CustomerMainActivity extends BaseShellActivity {

    @Override
    protected int navGraphRes() {
        return R.navigation.nav_customer;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_customer;
    }

    @Override
    protected int toolbarMenuRes() {
        return R.menu.menu_toolbar_customer;
    }

    @Override
    protected int[] topLevelDestinations() {
        return new int[]{R.id.homeFragment, R.id.transferFragment, R.id.utilitiesFragment,
                R.id.branchMapFragment, R.id.chatFragment};
    }

    @Override
    protected Role requiredRole() {
        return Role.CUSTOMER;
    }
}
