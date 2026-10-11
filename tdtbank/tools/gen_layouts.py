import os
import pathlib
OUT = str(pathlib.Path(__file__).resolve().parent.parent/'app'/'src'/'main'/'res'/'layout')
NS = 'xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto"'

def til(id, hint, inp='text', extra='', top=12, lines=1, maxlen=None):
    ml = f'\n            android:maxLength="{maxlen}"' if maxlen else ''
    return f'''
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/{id}"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="{top}dp"
            android:hint="{hint}"{extra}>

            <com.google.android.material.textfield.TextInputEditText
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="{inp}"{ml}
                android:maxLines="{lines}" />
        </com.google.android.material.textfield.TextInputLayout>'''

def dd(til_id, act_id, hint, top=12, vis=''):
    v = f'\n            android:visibility="{vis}"' if vis else ''
    return f'''
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/{til_id}"
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="{top}dp"
            android:hint="{hint}"{v}>

            <AutoCompleteTextView
                android:id="@+id/{act_id}"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="none" />
        </com.google.android.material.textfield.TextInputLayout>'''

def btn(id, text, style='', top=16, extra=''):
    st = f'\n            style="@style/{style}"' if style else ''
    return f'''
        <com.google.android.material.button.MaterialButton
            android:id="@+id/{id}"{st}
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="{top}dp"
            android:text="{text}"{extra} />'''

def tv(id, text='', style='', top=0, extra=''):
    t = f'\n            android:text="{text}"' if text else ''
    s = f'\n            android:textAppearance="{style}"' if style else ''
    return f'''
        <TextView
            android:id="@+id/{id}"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="{top}dp"{t}{s}{extra} />'''

def progress(id='progress'):
    return f'''
        <com.google.android.material.progressindicator.LinearProgressIndicator
            android:id="@+id/{id}"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:indeterminate="true"
            android:visibility="gone" />'''

def scroll(body):
    return f'''<?xml version="1.0" encoding="utf-8"?>
<ScrollView {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
{body}
    </LinearLayout>
</ScrollView>
'''

def write(name, content):
    with open(os.path.join(OUT, name + '.xml'), 'w', encoding='utf-8') as f:
        f.write(content)

H = '?attr/textAppearanceTitleLarge'
SEC = '?attr/textAppearanceTitleMedium'
SEC_COLOR = ''

# ---------- HOME ----------
write('fragment_home', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
{tv('tv_greeting', style=H)}
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/card_kyc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:visibility="gone"
        app:cardBackgroundColor="#FFF4D6">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="12dp">
{tv('tv_kyc_banner')}
            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_kyc"
                style="@style/Widget.Material3.Button.TextButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/kyc_banner_action" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:orientation="horizontal">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_deposit"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginEnd="4dp"
            android:layout_weight="1"
            android:text="@string/action_deposit" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_withdraw"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="4dp"
            android:layout_marginEnd="4dp"
            android:layout_weight="1"
            android:text="@string/action_withdraw" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_history"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="4dp"
            android:layout_weight="1"
            android:text="@string/title_history" />
    </LinearLayout>
{tv('tv_accounts_title', '@string/home_accounts', SEC, 16)}
{progress()}
{tv('tv_empty', '@string/home_no_accounts', '', 12, chr(10)+'            android:visibility="gone"')}
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_accounts"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1"
        android:clipToPadding="false" />
</LinearLayout>
''')

# ---------- ACCOUNT DETAIL ----------
write('fragment_account_detail', f'''<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
{tv('tv_type', style=H)}
{tv('tv_number', extra=chr(10)+'            android:textColor="@color/text_secondary"')}
{tv('tv_balance_label', top=12, extra=chr(10)+'            android:textColor="@color/text_secondary"')}
        <TextView
            android:id="@+id/tv_balance"
            style="@style/TextAppearance.TdtBank.Amount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />

        <LinearLayout
            android:id="@+id/info_container"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:orientation="vertical" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:orientation="horizontal">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_deposit"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginEnd="4dp"
                android:layout_weight="1"
                android:text="@string/action_deposit" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_withdraw"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:layout_marginEnd="4dp"
                android:layout_weight="1"
                android:text="@string/action_withdraw" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_transfer"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:layout_weight="1"
                android:text="@string/nav_transfer" />
        </LinearLayout>
{btn('btn_schedule', '@string/action_view_schedule', 'Widget.Material3.Button.TonalButton', 12, chr(10)+'            android:visibility="gone"')}
{btn('btn_pay_installment', '@string/action_pay_installment', '', 8, chr(10)+'            android:visibility="gone"')}
{tv('tv_history_title', '@string/title_history', SEC, 20)}
{tv('tv_no_tx', '@string/no_transactions', '', 8, chr(10)+'            android:textColor="@color/text_secondary"'+chr(10)+'            android:visibility="gone"')}
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv_transactions"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
''')

# ---------- TRANSFER ----------
write('fragment_transfer', scroll(
    tv('tv_title', '@string/nav_transfer', H) + progress() +
    dd('til_source', 'act_source', '@string/hint_source_account', 12) + '''
        <com.google.android.material.button.MaterialButtonToggleGroup
            android:id="@+id/toggle_type"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            app:selectionRequired="true"
            app:singleSelection="true">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_internal"
                style="?attr/materialButtonOutlinedStyle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/transfer_internal" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_interbank"
                style="?attr/materialButtonOutlinedStyle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/transfer_interbank" />
        </com.google.android.material.button.MaterialButtonToggleGroup>''' +
    dd('til_bank', 'act_bank', '@string/hint_bank', 12, 'gone') +
    til('til_recipient', '@string/hint_recipient_account', 'number', maxlen=16) +
    til('til_recipient_name', '@string/hint_recipient_name', 'textPersonName|textCapWords', ' ' ) .replace('android:hint="@string/hint_recipient_name" ', 'android:hint="@string/hint_recipient_name"\n            android:visibility="gone"') +
    til('til_amount', '@string/hint_amount', 'number', maxlen=12) +
    til('til_note', '@string/hint_note', 'text', maxlen=100) +
    tv('tv_fee_note', top=8, extra='\n            android:textColor="@color/text_secondary"') +
    btn('btn_submit', '@string/action_transfer')))

# ---------- CASH ----------
write('fragment_cash', scroll(
    tv('tv_heading', style=H) + progress() +
    dd('til_account', 'act_account', '@string/hint_account', 12) +
    dd('til_gateway', 'act_gateway', '@string/hint_gateway', 12) +
    tv('tv_gateway_note', top=4, extra='\n            android:textColor="@color/text_secondary"\n            android:textSize="12sp"') +
    til('til_amount', '@string/hint_amount', 'number', maxlen=12) +
    btn('btn_submit', '@string/action_confirm')))

# ---------- UTILITIES ----------
write('fragment_utilities', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="12dp">
{tv('tv_title', '@string/nav_utilities', H)}
{tv('tv_sub', '@string/utilities_sub', '', 4, chr(10)+'            android:textColor="@color/text_secondary"')}
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_utilities"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
''')

# ---------- UTILITY PAY ----------
write('fragment_utility_pay', scroll(
    '''
        <ImageView
            android:id="@+id/iv_icon"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:importantForAccessibility="no"
            app:tint="?attr/colorPrimary" />''' +
    tv('tv_heading', style=H, top=4) + progress() +
    tv('tv_sample_note', '@string/utility_sample_note', '', 8, '\n            android:textColor="@color/warning"\n            android:textSize="12sp"') +
    dd('til_account', 'act_account', '@string/hint_pay_from', 12) +
    dd('til_provider', 'act_provider', '@string/hint_provider', 12, 'gone') +
    til('til_reference', '@string/hint_customer_code', 'text', maxlen=20) +
    til('til_amount', '@string/hint_amount', 'number', maxlen=12) +
    dd('til_topup', 'act_topup', '@string/hint_topup_amount', 12, 'gone') +
    dd('til_item', 'act_item', '@string/hint_item', 12, 'gone') +
    til('til_note', '@string/hint_note', 'text', maxlen=60) +
    btn('btn_pay', '@string/action_pay')))

# ---------- HISTORY ----------
write('fragment_history', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_prev"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/prev_symbol" />

        <TextView
            android:id="@+id/tv_month"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:gravity="center"
            android:textAppearance="{SEC}" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_next"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/next_symbol" />
    </LinearLayout>
{tv('tv_summary', top=4, extra=chr(10)+'            android:gravity="center"')}
{progress()}
{tv('tv_empty', '@string/no_transactions', '', 16, chr(10)+'            android:gravity="center"'+chr(10)+'            android:textColor="@color/text_secondary"'+chr(10)+'            android:visibility="gone"')}
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_transactions"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
''')

write('fragment_mortgage_schedule', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="12dp">
{tv('tv_note', '@string/schedule_note', '', 0, chr(10)+'            android:textColor="@color/text_secondary"')}
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_schedule"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
''')

# ---------- MAP ----------
write('fragment_branch_map', f'''<?xml version="1.0" encoding="utf-8"?>
<FrameLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <org.osmdroid.views.MapView
        android:id="@+id/map"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <TextView
        android:id="@+id/tv_sample_banner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFF4D6"
        android:padding="8dp"
        android:text="@string/map_sample_banner"
        android:textSize="12sp"
        android:visibility="gone" />

    <com.google.android.material.progressindicator.LinearProgressIndicator
        android:id="@+id/progress"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:visibility="gone" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:orientation="vertical"
        android:padding="12dp">

        <com.google.android.material.card.MaterialCardView
            android:id="@+id/card_info"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:visibility="gone">

            <TextView
                android:id="@+id/tv_info"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="12dp" />
        </com.google.android.material.card.MaterialCardView>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_my_location"
                style="@style/Widget.Material3.Button.TonalButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginEnd="4dp"
                android:layout_weight="1"
                android:text="@string/map_my_location" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btn_nearest"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:layout_weight="1"
                android:text="@string/map_nearest" />
        </LinearLayout>
    </LinearLayout>
</FrameLayout>
''')

# ---------- CHAT ----------
def chip(id, text):
    return f'''
            <com.google.android.material.chip.Chip
                android:id="@+id/{id}"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="{text}" />'''
write('fragment_chat', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_messages"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:clipToPadding="false"
        android:padding="12dp" />

    <HorizontalScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:scrollbars="none">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:paddingStart="12dp"
            android:paddingEnd="12dp">
{chip('chip_balance', '@string/chip_balance')}
{chip('chip_month', '@string/chip_month')}
{chip('chip_electric', '@string/chip_electric')}
{chip('chip_branch', '@string/chip_branch')}
        </LinearLayout>
    </HorizontalScrollView>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:padding="8dp">

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="@string/chat_hint">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/et_message"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:imeOptions="actionSend"
                android:inputType="textCapSentences"
                android:maxLines="3" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_send"
            style="@style/Widget.Material3.Button.IconButton.Filled"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:contentDescription="@string/action_send"
            app:icon="@drawable/ic_send" />
    </LinearLayout>
</LinearLayout>
''')

# ---------- PROFILE ----------
write('fragment_profile', scroll(
    tv('tv_name', style=H) + tv('tv_username') + tv('tv_role') + tv('tv_national_id') + tv('tv_dob') +
    tv('tv_contact_title', '@string/profile_contact_title', SEC, 16) +
    til('til_phone', '@string/hint_phone', 'phone', maxlen=10) +
    til('til_email', '@string/hint_email', 'textEmailAddress') +
    til('til_address', '@string/hint_address', 'textPostalAddress|textMultiLine', lines=3) +
    btn('btn_save', '@string/action_save') + '''
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/card_kyc"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/profile_kyc_title"
                    android:textAppearance="?attr/textAppearanceTitleMedium" />

                <TextView
                    android:id="@+id/tv_kyc_status"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp" />

                <TextView
                    android:id="@+id/tv_kyc_note"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textColor="@color/negative"
                    android:visibility="gone" />

                <ImageView
                    android:id="@+id/iv_face"
                    android:layout_width="120dp"
                    android:layout_height="120dp"
                    android:layout_marginTop="8dp"
                    android:contentDescription="@string/face_preview_desc"
                    android:scaleType="centerCrop"
                    android:visibility="gone" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btn_kyc"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="@string/action_capture_kyc" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>''' +
    tv('tv_pw_title', '@string/profile_password_title', SEC, 20) +
    til('til_old_password', '@string/hint_old_password', 'textPassword', ' app:endIconMode="password_toggle"') +
    til('til_new_password', '@string/hint_new_password', 'textPassword', ' app:endIconMode="password_toggle"') +
    btn('btn_change_password', '@string/action_change_password', 'Widget.Material3.Button.TonalButton')))

# ---------- OFFICER ----------
write('fragment_customer_list', f'''<?xml version="1.0" encoding="utf-8"?>
<FrameLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">

        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/hint_search_customer">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/et_search"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text"
                android:maxLines="1" />
        </com.google.android.material.textfield.TextInputLayout>
{progress()}
{tv('tv_empty', '@string/no_customers', '', 16, chr(10)+'            android:gravity="center"'+chr(10)+'            android:visibility="gone"')}
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv_customers"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_marginTop="8dp"
            android:layout_weight="1"
            android:clipToPadding="false"
            android:paddingBottom="72dp" />
    </LinearLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_add"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="@string/title_create_customer"
        app:srcCompat="@drawable/ic_add" />
</FrameLayout>
''')

write('fragment_customer_create', scroll(
    tv('tv_title', '@string/title_create_customer', H) +
    tv('tv_hint', '@string/create_customer_hint', '', 4, '\n            android:textColor="@color/text_secondary"') +
    til('til_username', '@string/hint_username_new', 'textNoSuggestions', maxlen=32) +
    til('til_password', '@string/hint_initial_password', 'textPassword', ' app:endIconMode="password_toggle"') +
    til('til_full_name', '@string/hint_full_name', 'textPersonName|textCapWords', maxlen=100) +
    til('til_phone', '@string/hint_phone', 'phone', maxlen=10) +
    til('til_email', '@string/hint_email', 'textEmailAddress') +
    til('til_national_id', '@string/hint_national_id', 'number', maxlen=12) +
    til('til_dob', '@string/hint_dob', 'date', maxlen=10) +
    til('til_address', '@string/hint_address', 'textPostalAddress|textMultiLine', lines=3) +
    btn('btn_create', '@string/action_create_customer')))

write('fragment_customer_detail', scroll(
    tv('tv_header', style=H) + tv('tv_sub') + tv('tv_kyc') + tv('tv_status') +
    btn('btn_toggle_lock', '@string/action_lock', 'Widget.Material3.Button.OutlinedButton', 8) +
    tv('tv_edit_title', '@string/customer_edit_title', SEC, 16) +
    til('til_full_name', '@string/hint_full_name', 'textPersonName|textCapWords', maxlen=100) +
    til('til_phone', '@string/hint_phone', 'phone', maxlen=10) +
    til('til_email', '@string/hint_email', 'textEmailAddress') +
    til('til_national_id', '@string/hint_national_id', 'number', maxlen=12) +
    til('til_dob', '@string/hint_dob', 'date', maxlen=10) +
    til('til_address', '@string/hint_address', 'textPostalAddress|textMultiLine', lines=3) +
    btn('btn_save', '@string/action_save_changes') +
    btn('btn_capture_face', '@string/action_capture_kyc_counter', 'Widget.Material3.Button.TonalButton', 12) +
    tv('tv_accounts_title', '@string/home_accounts', SEC, 20) +
    btn('btn_open_account', '@string/action_open_account', 'Widget.Material3.Button.TonalButton', 8) + '''
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv_accounts"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp" />'''))

write('fragment_open_account', scroll(
    tv('tv_customer', style=H) +
    dd('til_type', 'act_type', '@string/hint_account_type', 12) +
    dd('til_term', 'act_term', '@string/hint_saving_term', 12) +
    tv('tv_no_policy', '@string/no_rate_policy', '', 8, '\n            android:textColor="@color/negative"\n            android:visibility="gone"') + '''
        <LinearLayout
            android:id="@+id/group_mortgage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:visibility="gone">''' +
    til('til_principal', '@string/hint_principal', 'number', maxlen=12) +
    til('til_rate', '@string/hint_loan_rate', 'numberDecimal', maxlen=5) +
    til('til_term_months', '@string/hint_loan_term', 'number', maxlen=3) +
    dd('til_frequency', 'act_frequency', '@string/hint_frequency', 12) + '''
        </LinearLayout>''' +
    btn('btn_open', '@string/action_open_account')))

write('fragment_kyc_queue', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
{tv('tv_title', '@string/kyc_queue_title', H)}
{tv('tv_hint', '@string/kyc_queue_hint', '', 4, chr(10)+'            android:textColor="@color/text_secondary"')}
{tv('tv_empty', '@string/kyc_queue_empty', '', 24, chr(10)+'            android:gravity="center"'+chr(10)+'            android:visibility="gone"')}
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_kyc"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
''')

write('fragment_rates', f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout {NS}
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
{tv('tv_title', '@string/nav_rates', H)}
{tv('tv_hint', '@string/rates_hint', '', 4, chr(10)+'            android:textColor="@color/text_secondary"')}
    <LinearLayout
        android:id="@+id/group_empty"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:visibility="gone">
{tv('tv_empty', '@string/rates_empty', '', 16)}
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_create_sample"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/action_create_sample_rates" />
    </LinearLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_rates"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
''')

write('item_customer', f'''<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView {NS}
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:clickable="true"
    android:focusable="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <TextView
            android:id="@+id/tv_name"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceTitleMedium" />

        <TextView
            android:id="@+id/tv_sub"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="@color/text_secondary" />

        <TextView
            android:id="@+id/tv_kyc"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="?attr/colorPrimary"
            android:textSize="12sp" />

        <TextView
            android:id="@+id/tv_locked"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/status_locked"
            android:textColor="@color/negative"
            android:textSize="12sp"
            android:visibility="gone" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
''')

write('item_kyc', f'''<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView {NS}
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp">

        <ImageView
            android:id="@+id/iv_face"
            android:layout_width="96dp"
            android:layout_height="96dp"
            android:contentDescription="@string/face_preview_desc"
            android:scaleType="centerCrop" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:id="@+id/tv_name"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textAppearance="?attr/textAppearanceTitleMedium" />

            <TextView
                android:id="@+id/tv_meta"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/text_secondary"
                android:textSize="12sp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal">

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btn_approve"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginEnd="4dp"
                    android:layout_weight="1"
                    android:text="@string/action_approve" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btn_reject"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="4dp"
                    android:layout_weight="1"
                    android:text="@string/action_reject" />
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
''')

write('item_rate', f'''<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView {NS}
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:clickable="true"
    android:focusable="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">

        <TextView
            android:id="@+id/tv_term"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textAppearance="?attr/textAppearanceTitleMedium" />

        <TextView
            android:id="@+id/tv_rate"
            style="@style/TextAppearance.TdtBank.Amount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
''')
print("layouts:", len(os.listdir(OUT)))
