@file:Suppress("SpellCheckingInspection")

package com.example.billboard

/*===================================================/
|| The Setting View where the user can change its
|| username, reset its password, sign out, change the
|| app theme.
|| The delete account is not yet implemented.
/====================================================*/

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billboard.ui.theme.Billboard_green
import com.example.billboard.ui.theme.Billboard_Red
import com.example.billboard.ui.theme.Billboard_lightGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsView (
    scState: ScaffoldState,
    navControl: NavController,
    userVM: UserViewModel,
    scope: CoroutineScope,
    auth: FirebaseAuth,
    themeStore: ThemePreference,
    themeSetting: Boolean
) {

    Scaffold(
        scaffoldState = scState,
        topBar = { TopBar(true, scState, false, scope ) },
        content = { SettingsContent( navControl, userVM, scope, auth, themeStore, themeSetting ) },
        drawerContent = { DrawerMainScreen ( navControl , scState, scope )
        }
    )

}


@Composable
fun SettingsContent( navControl: NavController, userVM: UserViewModel, scope: CoroutineScope, auth: FirebaseAuth, themeStore: ThemePreference, themeSetting: Boolean ) {

    val ddd = themeStore.getTheme.collectAsState(initial = Boolean)
    val checkedState = remember { mutableStateOf(ddd.value) }
    val openDialog = remember { mutableStateOf(false) }
    val emptyFieldAlert = remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(userVM.userName.value) }
    var editUserName by remember { mutableStateOf(false)}
    val deleteAcntAlert = remember { mutableStateOf(false)}

    //Function to store the new username
    fun saveUserName( newUserName: String ) {
        Firebase.firestore
            .collection("users")
            .document(userVM.user.value?.email.toString())
            .update("username", newUserName)
            .addOnSuccessListener {
                userVM.setUsername(newUserName)
                editUserName = false
            }
    }

    //Alert dialog to display confirmation message for password reset
    if (openDialog.value) {

        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = stringResource(R.string.passwd_reset))
            },
            text = {
                Text(text = stringResource(R.string.passwd_reset_mess) + " -> " + userVM.userEmail.value)
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        resetPassword(userVM)
                        openDialog.value = false
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(stringResource(R.string.send))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        openDialog.value = false
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    //Alert dialog to handle empty username field error
    if (emptyFieldAlert.value) {

        AlertDialog(
            onDismissRequest = {
                emptyFieldAlert.value = false
            },
            title = {
                Text(text = stringResource(R.string.error))
            },
            text = {
                Text(text = stringResource(R.string.all_inputs_required))
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        emptyFieldAlert.value = false
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    //Alert dialog for confirmation message when deleting the user account (not finished)
    if (deleteAcntAlert.value) {

        AlertDialog(
            onDismissRequest = {
                deleteAcntAlert.value = false
            },
            title = {
                Text(text = stringResource(R.string.confirm))
            },
            text = {
                Text(text = stringResource(R.string.conf_delete))
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteAcntAlert.value = false
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        //If the editUserName is false, the settings view chose the different settings option, otherwise it shows the edit username form
        if ( !editUserName ) {

            Spacer( modifier = Modifier.height(70.dp))

            Text( text = userVM.userName.value, fontSize = 20.sp )

            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                //Edit username button, display an edit form
                OutlinedButton(
                    onClick = {
                        editUserName = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource(R.string.edit_username))
                    }

                Spacer(modifier = Modifier.height(20.dp))

                //Reset password button, open an alert dialog
                OutlinedButton(
                    onClick = {
                        openDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource(R.string.reset_password))
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Sign out buttons, use the function from the UserViewModel
                OutlinedButton(
                    onClick = { userVM.signOut(auth) },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource( R.string.sign_out ))
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Theme switch that remember the last theme chosen
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text( text = stringResource(R.string.dark_mode))
                    Switch(
                        checked = themeSetting,
                        onCheckedChange = {
                            checkedState.value = it
                            scope.launch {
                                themeStore.saveTheme(it)
                            }
                            },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Billboard_green,
                            uncheckedThumbColor = Color.DarkGray,
                            checkedTrackColor = Billboard_lightGreen,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Redirection to MainScreenView button
                OutlinedButton(
                    onClick = {
                        navControl.navigate("MainScreen")
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource( R.string.exit_settings ))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Bottom
            ) {
                //Delete account button, open an alert dialog
                OutlinedButton(
                    onClick = {
                              deleteAcntAlert.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = Billboard_Red),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource( R.string.delete_account ))
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            //The edit username form is displayed
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text(text = stringResource(R.string.username)) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Billboard_green,
                        cursorColor = MaterialTheme.colors.onPrimary,
                        textColor = MaterialTheme.colors.onPrimary,
                        focusedLabelColor = MaterialTheme.colors.onPrimary

                    ),
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(64.dp),
                    shape = MaterialTheme.shapes.large
                )

                Spacer(modifier = Modifier.height(20.dp))

                //Submit button with empty field error handler
                OutlinedButton(
                    onClick = {
                        if(userName.isNotEmpty()) {
                            saveUserName(userName)
                        } else {
                            emptyFieldAlert.value = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text( text = stringResource(R.string.save))
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Cancel button
                OutlinedButton(
                    onClick = {
                        userName = userVM.userName.value
                        editUserName = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }
}

//Function to reset the user's password, done with the Firebase function
fun resetPassword(userVM: UserViewModel, email : String = ""){
    val useremail = email.ifEmpty { userVM.userEmail.value }
    FirebaseAuth.getInstance().sendPasswordResetEmail(useremail).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("Email sent to ", useremail)
        }
    }
}

