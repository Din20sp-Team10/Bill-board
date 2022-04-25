@file:Suppress("UNCHECKED_CAST", "SpellCheckingInspection")

package com.example.billboard

/*===================================================/
|| Edit members views with input for adding a new
|| member and the list of all the members with
|| buttons to edit, change admin status and delete
/====================================================*/

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.billboard.ui.theme.Billboard_green
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope

@Composable
fun AddEditMemberView(
    groupsVM: GroupsViewModel,
    userVM: UserViewModel,
    scState: ScaffoldState,
    scope: CoroutineScope,
    group: GroupClass,
    expenseNavControl: NavController,
    navControl : NavController
) {

    Scaffold(
        topBar = { TopBar(showMenu = false, scState, false, scope ) },
        content = { AddEditMemberContent( groupsVM, userVM, group, expenseNavControl, navControl) }
    )

}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AddEditMemberContent(
    groupsVM: GroupsViewModel,
    userVM: UserViewModel,
    group: GroupClass,
    expenseNavControl: NavController,
    navControl : NavController
) {

    var memberEmail by remember { mutableStateOf("") }
    var membersList by remember { mutableStateOf(group.members) }
    var adminsList by remember { mutableStateOf(group.admins) }
    var adminCheck by remember { mutableStateOf(false) }
    var editGroup by remember { mutableStateOf(group) }
    var newBalance by remember { mutableStateOf(group.balance) }

    val boolEdit = remember { mutableStateOf(false)}

    val existingMemberAlert = remember { mutableStateOf(false)}
    val validEmailAlert = remember { mutableStateOf(false)}
    val deleteMemberAlert = remember { mutableStateOf(false)}
    val emptyFieldAlert = remember { mutableStateOf(false)}
    val deleteMemConf = remember { mutableStateOf(false)}

    //Add member function
    fun addMember() {

        val newMemberBalanceMap = mutableMapOf<String, Double>()

        membersList.forEach { member ->
            val oldMemberBalance = editGroup.balance[member]!!
            newMemberBalanceMap[member] = 0.0
            oldMemberBalance[memberEmail] = 0.0
            newBalance[member] = oldMemberBalance
        }

        newBalance[memberEmail] = newMemberBalanceMap

        val tempMembers = mutableListOf<String>()
        membersList.forEach { member -> tempMembers.add(member) }
        tempMembers.add(memberEmail)
        membersList = tempMembers

        //If admin checkbox is checked, add to admin's list aswell
        if (adminCheck) {
            val tempAdmins = mutableListOf<String>()
            adminsList.forEach { admin -> tempAdmins.add(admin) }
            tempAdmins.add(memberEmail)
            adminsList = tempAdmins
        }

        val newGroup =
            GroupClass(adminsList, group.expenses, membersList, group.name, newBalance, group.id)

        editGroup = newGroup
        newBalance = newGroup.balance
        groupsVM.editGroup(newGroup, userVM, "Group member added")
        memberEmail = ""
    }

    //Edit a member email function
    fun editMemberName(member: String, editedMember : String){
        // Edit in group collection//

        // Apply changes in group balance //
        membersList.forEach { m ->
            if(m == member){
                //Adding new key in map//
                val oldMemberBalance = editGroup.balance[m]!!
                newBalance[editedMember] = oldMemberBalance
                //Deleting old key in map//
                newBalance.remove(m)
            } else {
                //In each member balance, adding new key with old value
                val oldBalance = editGroup.balance[m]!!.getValue(member)
                newBalance[m]?.set(editedMember, oldBalance)

                //Deleting old key balance//
                newBalance[m]?.remove(member)
            }
        }

        // Apply changes in members list //

        val tempMembers = mutableListOf<String>()
        membersList.forEach { m -> if(m != member) tempMembers.add(m) }
        tempMembers.add(editedMember)
        membersList = tempMembers

        // Apply changes in admins list //

        if(adminsList.contains(member)){
            val tempAdmins = mutableListOf<String>()
            adminsList.forEach { admin -> if(admin != member) tempAdmins.add(admin) }
            tempAdmins.add(editedMember)
            adminsList = tempAdmins
        }

        val newGroup = GroupClass(
            adminsList,
            group.expenses,
            membersList,
            group.name,
            newBalance,
            group.id
        )

        // Edit in expense collection //
        // In each expense, edit member email in rest list or payer and/or paidvalues map //

        group.expenses.forEach { exp ->
            val fexp = Firebase.firestore.collection("expenses").document(exp)

            fexp.get()
                .addOnSuccessListener { expenseSnapshot ->
                    val oldExpense = ExpenseClass(
                        expenseSnapshot.get("name").toString(),
                        expenseSnapshot.get("amount") as Double,
                        expenseSnapshot.get("payer").toString(),
                        expenseSnapshot.get("date").toString(),
                        expenseSnapshot.get("groupid").toString(),
                        expenseSnapshot.get("rest") as MutableList<String>,
                        expenseSnapshot.get("expid").toString(),
                        expenseSnapshot.get("paidvalues") as MutableMap<String, Boolean>,
                        expenseSnapshot.get("receiptURL").toString()
                    )

                    //Edit payer//
                    if(member == oldExpense.payer){
                        fexp.update("payer",editedMember)
                    }

                    //Edit rest//
                    if(oldExpense.rest.contains(member)) {
                        fexp.update("rest", FieldValue.arrayRemove(member))
                        fexp.update("rest", FieldValue.arrayUnion(editedMember))
                    }

                    //Edit paid values//
                    if(oldExpense.paidvalues.containsKey(member)){
                        val newPaidValuesMap = mutableMapOf<String,Boolean>()
                        oldExpense.paidvalues.forEach { other ->
                            newPaidValuesMap[other.key] = other.value
                        }
                        newPaidValuesMap[editedMember] = newPaidValuesMap[member]!!
                        newPaidValuesMap.remove(member)

                        Log.d("newPaidValuesMap",newPaidValuesMap.toString())

                        fexp.update("paidvalues", newPaidValuesMap)
                        boolEdit.value = false
                    }
                }
        }

        editGroup = newGroup

        groupsVM.editGroup(newGroup, userVM, "Group member change email")
        memberEmail = ""
        groupsVM.getGroups()
        navControl.navigate(group.id)
    }

    // Function to add a member to the admin group list
    fun makeAdmin() {

        val tempAdmins = mutableListOf<String>()
        adminsList.forEach { admin -> tempAdmins.add(admin) }
        tempAdmins.add(memberEmail)
        adminsList = tempAdmins

        val newGroup = GroupClass(
            adminsList,
            group.expenses,
            group.members,
            group.name,
            group.balance,
            group.id
        )

        memberEmail = ""
        editGroup = newGroup
        groupsVM.editGroup(newGroup, userVM, "Group member added as admin")
    }

    //Function to delete a member from the admin group list
    fun deleteAdmin() {

        val tempAdmins = mutableListOf<String>()
        adminsList.forEach { admin -> tempAdmins.add(admin) }
        tempAdmins.remove(memberEmail)
        adminsList = tempAdmins

        val newGroup = GroupClass(
            adminsList,
            group.expenses,
            group.members,
            group.name,
            group.balance,
            group.id
        )

        memberEmail = ""
        editGroup = newGroup
        groupsVM.editGroup( newGroup, userVM, "Group member removed admin")
    }

    //Function to check if a member's balance is clear
    fun isMemberBalanceClear(
        member: String
    ): Boolean {
        group.balance[member]?.forEach { other ->
            if (other.value != 0.0) return false
        }
        return true
    }

    //Function to delete a member from group
    fun deleteMember() {

        /* Delete members line from group balance */
        membersList.forEach { m ->
            if(m == memberEmail){
                newBalance.remove(m)
            } else {
                //In each member balance, remove member balance
                newBalance[m]?.remove(memberEmail)
            }
        }

        /* Delete from admins */
        if(group.admins.contains(memberEmail)){
            val tempAdmins = mutableListOf<String>()
            adminsList.forEach { admin -> if(admin != memberEmail) tempAdmins.add(admin) }
            adminsList = tempAdmins
        }

        /* Delete from members */
        val tempMembers = mutableListOf<String>()
        membersList.forEach { m -> if(m != memberEmail) tempMembers.add(m) }
        membersList = tempMembers


        val newGroup = GroupClass(
            adminsList,
            group.expenses,
            membersList,
            group.name,
            newBalance,
            group.id
        )

        editGroup = newGroup
        groupsVM.editGroup( newGroup, userVM, "Group member deleted")
        memberEmail = ""

    }

    //Using if statement to display input field to modify an user's email if the edit button is pressed
    if(!boolEdit.value) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(text = group.name, fontSize = 30.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Divider(
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(1.dp),
                    color = Billboard_green
                )

                Spacer(modifier = Modifier.height(15.dp))

                //Input to enter a new member email
                OutlinedTextField(
                    value = memberEmail,
                    onValueChange = { memberEmail = it },
                    label = { Text(text = stringResource(R.string.member_email)) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.onPrimary,
                        cursorColor = MaterialTheme.colors.onPrimary,
                        textColor = MaterialTheme.colors.onPrimary,
                        focusedLabelColor = MaterialTheme.colors.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(64.dp),
                    shape = MaterialTheme.shapes.large,
                    textStyle = TextStyle(color = MaterialTheme.colors.onPrimary)
                )

                Spacer(modifier = Modifier.height(15.dp))

                //Checkbox to add the new member as an admin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = stringResource(R.string.add_as_admin))

                    Checkbox(
                        checked = adminCheck,
                        onCheckedChange = { adminCheck = it },
                        colors = CheckboxDefaults.colors(MaterialTheme.colors.onPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                //Submit button with errors handling : existing member, not valid email, empty field
                OutlinedButton(
                    onClick = {
                        if (group.members.contains(memberEmail)) {
                            existingMemberAlert.value = true
                        } else if (!EmailValidator.isEmailValid(memberEmail)) {
                            validEmailAlert.value = true
                        } else if (memberEmail.isEmpty()){
                            emptyFieldAlert.value = true
                        } else {
                            addMember()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text(text = stringResource(R.string.add_member))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Divider(
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(1.dp),
                    color = Billboard_green
                )

                Spacer(modifier = Modifier.height(20.dp))

            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(.8f)
                    .verticalScroll(enabled = true, state = ScrollState(1))
            ) {
                membersList.forEach { member ->
                    Text(text = member, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        //An user can't edit himself as he will be kicked out
                        if (userVM.userEmail.value != member) {
                                OutlinedButton(
                                    onClick = {
                                        memberEmail = member
                                        boolEdit.value = true },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(35.dp),
                                    shape = MaterialTheme.shapes.large,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary ),
                                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                                ) {
                                    Text(text = stringResource(R.string.edit))
                                }
                        }

                        //Make admin or remove admin buttons depends on the current user status
                        if (!group.admins.contains(member)) {
                            OutlinedButton(
                                onClick = {
                                    memberEmail = member
                                    makeAdmin()
                                },
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(35.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary ),
                                elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                            ) {
                                Text(text = stringResource(R.string.make_admin))

                            }
                        }

                        if (group.admins.contains(member) && userVM.userEmail.value != member) {
                            OutlinedButton(
                                onClick = {
                                    memberEmail = member
                                    deleteAdmin()
                                },
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(35.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary ),
                                elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                            ) {
                                Text(text = stringResource(R.string.rem_admin))

                            }
                        }

                        //User cannot delete themself, they have to leave the group
                        if (userVM.userEmail.value != member) {
                            OutlinedButton(
                                onClick = { if(isMemberBalanceClear(member)) { memberEmail = member; deleteMemConf.value = true } else {deleteMemberAlert.value = true} },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(35.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary),
                                elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                            ) {
                                Text(text = stringResource(R.string.delete))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(.75f)
                            .height(1.dp),
                        color = Billboard_green
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        expenseNavControl.navigate("group")
                    },
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                        .height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary ),
                    elevation = ButtonDefaults.elevation(7.dp, 5.dp, 0.dp)
                ) {
                    Text(text = stringResource(R.string.exit))
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
        }
        }
        //Alert dialog when trying to add existing member
        if(existingMemberAlert.value){
            AlertDialog(
                onDismissRequest = {
                    existingMemberAlert.value = false
                },
                title = {
                    Text(text = stringResource(R.string.error))
                },
                text = {
                    Text(text = stringResource(R.string.err_add_member))
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            existingMemberAlert.value = false
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        //Alert dialog when trying to add an user with incorrect formed email
        if(validEmailAlert.value){
            AlertDialog(
                onDismissRequest = {
                    validEmailAlert.value = false
                },
                title = {
                    Text(text = stringResource(R.string.error))
                },
                text = {
                    Text(text = stringResource(R.string.not_valid_email))
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            validEmailAlert.value = false
                            memberEmail = ""
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            )
        }

        //Alert dialog if delete a member with not clear balance
        if(deleteMemberAlert.value){
            AlertDialog(
                onDismissRequest = {
                    deleteMemberAlert.value = false
                },
                title = {
                    Text(text = stringResource(R.string.error))
                },
                text = {
                    Text(text = stringResource(R.string.err_del_member))
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            deleteMemberAlert.value = false
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        //Alert dialog when input field are empty
        if(emptyFieldAlert.value){
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
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        //Alert dialog when deleting user
        if(deleteMemConf.value){
            AlertDialog(
                onDismissRequest = {
                    deleteMemConf.value = false
                },
                title = {
                    Text(text = stringResource(R.string.delete_conf))
                },
                text = {
                    Text(text = stringResource(R.string.delete_m_mess) + " " + memberEmail)
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            deleteMember()
                            deleteMemConf.value = false
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            deleteMemConf.value = false
                        },
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }
    } else {
        //Edit member username form
        val oldMember by remember { mutableStateOf(memberEmail) }
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(enabled = true, state = ScrollState(1)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            OutlinedTextField(
                value = memberEmail,
                onValueChange = { memberEmail = it },
                label = { Text(text = stringResource(R.string.member_email)) },
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

            //Submit button with errors checks : valid email and empty field
            OutlinedButton(
                onClick = {
                    if (EmailValidator.isEmailValid(memberEmail)) {
                        if (group.members.contains(memberEmail)) {
                            existingMemberAlert.value = true
                        } else {
                        editMemberName(
                            oldMember,
                            memberEmail)
                        boolEdit.value = false
                    }} else {
                        boolEdit.value = false
                        validEmailAlert.value = true
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

            OutlinedButton(
                onClick = {
                    boolEdit.value = false
                    memberEmail = ""
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