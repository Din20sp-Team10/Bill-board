package com.example.billboard

/*===================================================/
|| The MainScreen display the group list where the
|| user is member.
/====================================================*/

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billboard.ui.theme.Billboard_green
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainScreen(
    navControl: NavController,
    groups: List<GroupClass>,
    scState: ScaffoldState,
    scope: CoroutineScope
) {

    Scaffold(
        scaffoldState = scState,
        topBar = { TopBar(true, scState, false, scope) },
        bottomBar = { BottomBarMainScreen( navControl ) },
        content = { MainScreenContent( navControl, groups) },
        drawerContent = { DrawerMainScreen (navControl, scState, scope,)
        }
    )
}

@Composable
fun MainScreenContent( navControl: NavController, groups: List<GroupClass>) {

    Column(
        Modifier.fillMaxSize(),
    ){
        //Group cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(20.dp))

            Text( text = stringResource( R.string.your_groups), fontSize = 25.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth(.83f),
                color = Billboard_green
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxSize(.82f)
                .verticalScroll(enabled = true, state = ScrollState(1)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (groups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(.7f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(230.dp))
                    Text(text = "Welcome to BillBoard", fontSize = 25.sp, textAlign = TextAlign.Center )
                    Text(text = "Create a group from the button below and start tracking", fontSize = 18.sp, textAlign = TextAlign.Center )
                }
            }

            //Each group name is clickable and redirect to the corresponding group view
            groups.forEach { group ->
                Spacer(modifier = Modifier.height(5.dp))

                OutlinedButton(
                    onClick = { navControl.navigate( group.id ) },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.onPrimary,
                    ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text(text = group.name )
                }
            }
        }
    }
}

