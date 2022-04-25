package com.example.billboard


/*===================================================/
|| This view is either ADD or EDIT, depending on the
|| expense that is passed to it. Adding new expense
|| passes an empty expense class, but when navigated
|| from an existing expense page, it passes the expense
|| from firebase.
/====================================================*/

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billboard.ui.theme.Billboard_green
import kotlinx.coroutines.CoroutineScope

@Composable
fun AddEditExpenseView(
    groupInfo: GroupClass,
    expenseNavControl: NavController,
    expensesViewModel: ExpensesViewModel,
    expense : ExpenseClass,
    scState: ScaffoldState,
    groupsVM: GroupsViewModel,
    scope: CoroutineScope,
    userVM: UserViewModel
) {
    Scaffold(
        topBar = { TopBar(showMenu = false, scState, false, scope) },
        content = {
            AddEditExpenseViewContent(
                groupInfo = groupInfo,
                expenseNavControl = expenseNavControl,
                expensesViewModel = expensesViewModel,
                expense = expense,
                groupsVM = groupsVM,
                userVM
            )
        }
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AddEditExpenseViewContent(
                   groupInfo: GroupClass,
                   expenseNavControl: NavController,
                   expensesViewModel: ExpensesViewModel,
                   expense : ExpenseClass,
                   groupsVM: GroupsViewModel,
                   userVM: UserViewModel
) {

    val newExpense by remember { mutableStateOf( expense ) }
    var menuExpanded by remember { mutableStateOf(false) }
    val groupMembers by remember { mutableStateOf(groupInfo.members) }
    var expenseName by remember { mutableStateOf(expense.name)}
    var expenseAmount by remember { mutableStateOf(expense.amount.toString())}
    var payerMember: String by remember { mutableStateOf(expense.payer) }
    val membersWhoPay by remember {mutableStateOf(expense.rest)}
    val openDialog = remember { mutableStateOf(false) }
    val dialogInvalidAmnt = remember { mutableStateOf(false) }
    val dialogAmountTooLittle = remember { mutableStateOf(false)}
    var payerButtonText by remember { mutableStateOf("")}


    //////////////////////
    // Main container  //
    ////////////////////
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ///////////////////////////////
        // Upper part of the screen //
        /////////////////////////////
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //////////////////////////////
            // Head line on the screen //
            ////////////////////////////
            if (expense.expid.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.edit_expense),
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp
                )
            } else {
                Text(
                    text = stringResource(R.string.add_expense),
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ///////////////////////////////
            // Expense name input field //
            /////////////////////////////
            OutlinedTextField(
                value = expenseName,
                onValueChange = { expenseName = it },
                label = { Text(text = stringResource(R.string.expense_name)) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Billboard_green,
                    cursorColor = MaterialTheme.colors.onPrimary,
                    textColor = MaterialTheme.colors.onPrimary,
                    focusedLabelColor = MaterialTheme.colors.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(.75f)
                    .height(64.dp)
                    .padding(0.dp),
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(20.dp))

            /////////////////////////////////
            // Expense amount input field //
            ///////////////////////////////
            OutlinedTextField(
                value = expenseAmount,
                onValueChange = { expenseAmount = it },
                label = { Text(text = stringResource(R.string.expense_amount)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Billboard_green,
                    cursorColor = MaterialTheme.colors.onPrimary,
                    textColor = MaterialTheme.colors.onPrimary,
                    focusedLabelColor = MaterialTheme.colors.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(.75f)
                    .height(64.dp)
                    .padding(0.dp),
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Text( text = stringResource(R.string.payer_member))
                Spacer(modifier = Modifier.height(10.dp))

                ///////////////////////
                // Drop down button //
                /////////////////////
                OutlinedButton(
                    onClick = { menuExpanded = !menuExpanded },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    if (payerMember.isEmpty()){
                        payerButtonText = stringResource(R.string.select)
                    } else {
                        val uname = remember { mutableStateOf("default")}
                        getUsername(payerMember, uname)
                        if(uname.value == "null") uname.value = payerMember.substringBefore("@")
                        payerButtonText = uname.value
                    }
                    Text(text = payerButtonText )
                    Icon(Icons.Filled.ArrowDropDown, "Arrow for dropdownmenu" )
                }

                /////////////////////
                // Drop down menu //
                ///////////////////
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                ) {
                    groupMembers.forEach { member ->
                        val uname = remember { mutableStateOf("default")}
                        getUsername(member, uname)
                        if(uname.value == "null") uname.value = member.substringBefore("@")
                        DropdownMenuItem(onClick = {
                            if(payerMember.isNotEmpty()) {
                                membersWhoPay.add(payerMember)
                            }
                            payerMember = member
                            if (membersWhoPay.contains(payerMember)) {
                                membersWhoPay.remove(
                                    payerMember
                                )
                            }
                            menuExpanded = !menuExpanded
                        }) {
                            Text(text = uname.value)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(fraction = 0.75f),
                horizontalAlignment = Alignment.Start
            ){

                Text(text = stringResource(R.string.rest))
                Spacer(modifier = Modifier.height(10.dp))

                //////////////////////////////////////////////
                // Scrolling column containing the members //
                ////////////////////////////////////////////
                Column( modifier = Modifier
                    .fillMaxHeight(.5f)
                    .verticalScroll(enabled = true, state = ScrollState(1))
                ) {
                    groupMembers.forEach { member ->
                        val uname = remember { mutableStateOf("default")}
                        getUsername(member, uname)
                        if(uname.value == "null") uname.value = member.substringBefore("@")
                        if (member != payerMember) {
                            Row {
                                CheckBox(member, membersWhoPay, expense)
                                Text(uname.value)
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //////////////////////
            // Add/Edit button //
            ////////////////////
            Button(modifier = Modifier
                .fillMaxWidth(.75f)
                .height(40.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary ),
                onClick = {
                    if (expenseName.isNotEmpty() && expenseAmount != "0.0" && payerMember.isNotEmpty() && membersWhoPay.isNotEmpty()) {
                        if(expenseAmount.toDoubleOrNull() == null) {
                            dialogInvalidAmnt.value = true
                        } else {
                            if (expenseAmount.toDouble() / (membersWhoPay.size + 1) < 0.01) {
                                dialogAmountTooLittle.value = true
                            } else {
                                if (expenseAmount.toDouble() / (membersWhoPay.size + 1) < 0.01) {
                                    dialogAmountTooLittle.value = true
                                } else {
                                    newExpense.name = expenseName
                                    newExpense.amount = expenseAmount.toDouble()
                                    newExpense.payer = payerMember
                                    newExpense.rest = membersWhoPay
                                    if (expense.expid.isEmpty()) {
                                        expensesViewModel.addExpenseLine(
                                            newExpense,
                                            expenseNavControl,
                                            groupInfo,
                                            groupsVM,
                                            userVM
                                        )
                                    } else {
                                        expensesViewModel.editExpenseLine(
                                            expenseNavControl,
                                            groupInfo,
                                            groupsVM,
                                            newExpense,
                                            userVM
                                        )
                                    }
                                }
                            }
                        }
                    } else { openDialog.value = true }
                }
            ){
                if (expense.expid.isNotEmpty()) {
                    Text(text = stringResource(R.string.edit))
                } else {
                    Text(text = stringResource(R.string.add_expense))
                }
            }

            /////////////////////////
            // Input error Dialog //
            ///////////////////////
            if (openDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openDialog.value = false
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
                                openDialog.value = false
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary )
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }


            //////////////////////////
            // Amount error Dialog //
            ////////////////////////
            if (dialogAmountTooLittle.value) {
                AlertDialog(
                    onDismissRequest = {
                        dialogAmountTooLittle.value = false
                    },
                    title = {
                        Text(text = stringResource(R.string.error))
                    },
                    text = {
                        Text(text = stringResource(R.string.amount_too_short))
                    },
                    confirmButton = {
                        OutlinedButton(
                            onClick = {
                                dialogAmountTooLittle.value = false
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary )
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }
            if (dialogInvalidAmnt.value) {
                AlertDialog(
                    onDismissRequest = {
                        dialogAmountTooLittle.value = false
                    },
                    title = {
                        Text(text = stringResource(R.string.error))
                    },
                    text = {
                        Text(text = stringResource(R.string.amount_invalid))
                    },
                    confirmButton = {
                        OutlinedButton(
                            onClick = {
                                dialogInvalidAmnt.value = false
                            },
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.outlinedButtonColors( contentColor = MaterialTheme.colors.onPrimary )
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }
        }

        ////////////////////////////
        // Bottom navigation bar //
        //////////////////////////
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth(.83f),
                color = Billboard_green
            )
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back icon",
                    modifier = Modifier
                        .clickable { expenseNavControl.navigate(expense.expid) }
                        .padding(35.dp, 20.dp)
                )
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////
// This composable is used to add/remove people from the expense participants list //
////////////////////////////////////////////////////////////////////////////////////
@Composable
fun CheckBox(member : String, membersWhoPay : MutableList<String>, expense : ExpenseClass){
    val checkState = remember {mutableStateOf(false)}
    if( expense.expid.isNotEmpty() && expense.rest.contains(member)){
        checkState.value = true
    }

    Checkbox(
        checked = checkState.value,
        onCheckedChange = { checkState.value = it
            if(checkState.value) {
                membersWhoPay.add(member)
            } else {
                membersWhoPay.remove(member)
            }
        },
        colors = CheckboxDefaults.colors(MaterialTheme.colors.onPrimary)
    )
}
