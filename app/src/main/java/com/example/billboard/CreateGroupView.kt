package com.example.billboard

/*===================================================/
|| Create new group view
/====================================================*/

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.billboard.ui.theme.Billboard_green
import kotlinx.coroutines.CoroutineScope

@Composable
fun CreateGroupView( groupsVM: GroupsViewModel, navControl: NavController, scState: ScaffoldState, scope: CoroutineScope, userVM: UserViewModel) {

    Scaffold(
        topBar = { TopBar(showMenu = false, scState, false, scope ) },
        content = { CreateGroupContent( groupsVM, navControl, userVM ) }
    )

}

@Composable
fun CreateGroupContent( groupsVM: GroupsViewModel, navControl: NavController, userVM: UserViewModel ) {

    var groupName by remember { mutableStateOf("")}

    ///////////////////////
    // Container column //
    /////////////////////
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(150.dp))

        /////////////////////////////
        // Group name input field //
        ///////////////////////////
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text(text = stringResource(R.string.group_name)) },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Billboard_green,
                cursorColor = MaterialTheme.colors.onPrimary,
                textColor = MaterialTheme.colors.onPrimary,
                focusedLabelColor = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(.75f),
            shape = MaterialTheme.shapes.large,
            textStyle = TextStyle(color = MaterialTheme.colors.onPrimary)
        )

        Spacer(modifier = Modifier.height(15.dp))

        //////////////////////////
        // Create group button //
        ////////////////////////
        OutlinedButton(
            onClick = {
                groupsVM.createGroup( groupName, navControl, userVM )
            },
            modifier = Modifier
                .fillMaxWidth(.75f)
                .height(40.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary),
            elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
        ) {
            Text(text = stringResource(R.string.create_group))
        }

        Spacer(modifier = Modifier.height(15.dp))

        ////////////////////
        // Cancel button //
        //////////////////
        OutlinedButton(
            onClick = {
                navControl.navigate("MainScreen")
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