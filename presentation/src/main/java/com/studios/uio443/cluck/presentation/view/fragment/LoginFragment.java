package com.studios.uio443.cluck.presentation.view.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.studios.uio443.cluck.presentation.R;
import com.studios.uio443.cluck.presentation.mvp.LoginFragmentVP;
import com.studios.uio443.cluck.presentation.presenter.LoginFragmentPresenter;
import com.studios.uio443.cluck.presentation.presenter.PresenterManager;
import com.studios.uio443.cluck.presentation.structure.router.LoginRouter;
import com.studios.uio443.cluck.presentation.util.Consts;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import javax.inject.Inject;

/**
 * Created by zundarik
 */

public class LoginFragment extends BaseFragment implements LoginFragmentVP.View {

	private static final String LOGIN = "LOGIN";
	private static final String PASSWORD = "PASSWORD";
	private static final String[] sMyScope = new String[]{
					VKScope.FRIENDS,
					VKScope.WALL,
					VKScope.PHOTOS,
					VKScope.NOHTTPS,
					VKScope.MESSAGES,
					VKScope.DOCS
	};
	@Inject
	LoginRouter router;
	LoginFragmentPresenter presenter;
	//используем butterknife
	//https://jakewharton.github.io/butterknife/
	//Обзор Butter Knife - https://startandroid.ru/ru/blog/470-butter-knife.html
	@BindView(R.id.login_email_layout)
	TextInputLayout loginEmailLayout;
	@BindView(R.id.login_password_layout)
	TextInputLayout loginPasswordLayout;
	@BindView(R.id.login_email_input)
	EditText loginEmailInput;
	@BindView(R.id.login_password_input)
	EditText loginPasswordInput;
	@BindView(R.id.sign_in_button_VK)
	ImageButton btnSignInVK;
	@BindView(R.id.btn_login)
	Button btnLogin;
	@BindView(R.id.link_signup)
	TextView linkSignUp;

	public LoginFragment() {
		super();
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_login;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Log.d(Consts.TAG, "LoginFragment.onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);

		//TODO чтение последнего логина из префа

		if (savedInstanceState == null) {
			//presenter = new LoginFragmentPresenter();
		} else {
			loginEmailInput.setText(savedInstanceState.getString(LOGIN, ""));
			loginPasswordInput.setText(savedInstanceState.getString(LOGIN, ""));
			presenter = PresenterManager.getInstance().restorePresenter(savedInstanceState);
		}
		presenter.bindView(this);

		//Если у пользователя не установлено приложение ВКонтакте,
		// то SDK будет использовать авторизацию через новую Activity при помощи OAuth.
		btnSignInVK.setOnClickListener(v -> presenter.onSignInVK());

		btnLogin.setOnClickListener(v -> {
			String email = loginEmailInput.getText().toString();
			String password = loginPasswordInput.getText().toString();

			presenter.onLogin(email, password);
		});

		linkSignUp.setOnClickListener(v -> router.showSignupFragment(presenter));
	}

	@Override
	public void onResume() {
		Log.d(Consts.TAG, "LoginFragment.onResume");
		super.onResume();

		//TODO запись последнего логина в преф
		presenter.bindView(this);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		Log.d(Consts.TAG, "LoginFragment.onSaveInstanceState");
		super.onSaveInstanceState(outState);

		outState.putString(LOGIN, loginEmailInput.getText().toString());
		outState.putString(PASSWORD, loginPasswordInput.getText().toString());
		PresenterManager.getInstance().savePresenter(presenter, outState);
	}

	@Override
	public void onPause() {
		Log.d(Consts.TAG, "LoginFragment.onPause");
		super.onPause();

		presenter.unbindView();
	}

	@Override
	public void showProgressDialog() {
		btnLogin.setEnabled(true);
		final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage(getString(R.string.authenticating));
		progressDialog.show();

		new android.os.Handler().postDelayed(
						() -> {
							// On complete call either onLoginSuccess or onLoginFailed

							btnLogin.setEnabled(true);
							router.showLoginPinActivity();
							// onLoginFailed();
							progressDialog.dismiss();
						}, 3000);
	}

	@Override
	public void showLoginFailed() {
		Log.e(Consts.TAG, "LoginFragment.showLoginFailed");
		Toast.makeText(getActivity(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();

		btnLogin.setEnabled(false);
	}

	@Override
	public boolean validate() {
		boolean valid = true;

		String email = loginEmailInput.getText().toString();
		String password = loginPasswordInput.getText().toString();

		if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			loginEmailInput.setError(getString(R.string.enter_valid_email));
			valid = false;
		} else {
			loginEmailInput.setError(null);
		}

		if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
			loginPasswordInput.setError(getString(R.string.password_length_error));
			valid = false;
		} else {
			loginPasswordInput.setError(null);
		}

		return valid;
	}

	@Override
	public void VKSdkLogin() {
		VKSdk.login(getActivity(), sMyScope);
	}
}
