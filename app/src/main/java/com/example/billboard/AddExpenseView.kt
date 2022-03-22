import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.billboard.*
import com.example.billboard.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.math.exp


@Composable

fun AddEditExpenseView(
                   groupInfo: DocumentSnapshot,
                   expenseNavControl: NavController,
                   expensesViewModel: ExpensesViewModel,
                   expense : ExpenseClass) {

    var menuExpanded by remember { mutableStateOf(false) }
    var dropDownWidth by remember { mutableStateOf(0) }

    val groupMembers = remember { mutableStateOf(listOf<String>()) }
    getGroupMembers(groupInfo.id, groupMembers)

    var newExpense : ExpenseClass = expensesViewModel.createExpense(groupid = groupInfo.id)

    var fieldError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var expenseName by remember { mutableStateOf("")}
    var expenseAmount by remember { mutableStateOf("")}
    var payerMember: String by remember { mutableStateOf("") }
    val membersWhoPay = remember {mutableStateListOf<String>()}


    /* TODO EDIT MODE
    if(id.isNotEmpty()){
        expenseName = name
        expenseAmount = amount
        payerMember = payer
        rest.split(",").forEach { member ->
            membersWhoPay.add(member)
        }
        }

     */

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "BillBoard")
        if(expense.expid.isNotEmpty()){
            Text(text = "Edit an new expense line")
        } else {
            Text(text = "Add a new expense line")
        }
        OutlinedTextField(value = expenseName, onValueChange = { expenseName = it}, label = { Text(text = "Expense name") })
        OutlinedTextField(value = expenseAmount, onValueChange = {expenseAmount = it}, label = { Text(text = "Expense amount") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
        Column() {
            OutlinedTextField(
                value = payerMember,
                onValueChange = { payerMember = it },
                modifier = Modifier
                    .onSizeChanged {
                        dropDownWidth = it.width
                    },
                label = { Text("Payer member") },
                trailingIcon = {
                    Icon(Icons.Filled.ArrowDropDown, "Arrow for dropdownmenu",
                        Modifier.clickable { menuExpanded = !menuExpanded })
                }
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { dropDownWidth.toDp() })
            ) {
                groupMembers.value.forEach { member ->
                    DropdownMenuItem(onClick = {
                        payerMember = member
                        if(membersWhoPay.contains(payerMember)) membersWhoPay.remove(payerMember)
                    }) {
                        Text(text = member)
                    }
                }
            }
        }

        Text(text = "Members who have to pay")
        groupMembers.value.forEach { member ->
            if (member != payerMember) {
                Row() {
                    CheckBox(member, membersWhoPay)
                    Text(member)
                }
            }
        }

        Button(onClick = {
            if(expenseName.isNotEmpty() && expenseAmount.isNotEmpty() && payerMember.isNotEmpty() && membersWhoPay.isNotEmpty()){
                expense.name = expenseName
                expense.amount = expenseAmount.toDouble()
                expense.payer = payerMember
                expense.rest = membersWhoPay

                expensesViewModel.addExpenseLine(
                        expense,
                        expenseNavControl
                    )
                }
        else {fieldError = true}}){
            if(expense.expid.isNotEmpty()){
                Text(text = "Edit")
            } else {
                Text(text = "Add a new expense line")
            }
        }
        if(fieldError) {
            Text(text = errorMessage, fontSize = 24.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Row(
            horizontalArrangement = Arrangement.Start
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "back icon",
                modifier = Modifier.clickable {  expenseNavControl.navigate("group")  })
        }
    }
}

@Composable
fun CheckBox(member : String, membersWhoPay : MutableList<String>){
    val checkState = remember {mutableStateOf(false)}
    Checkbox(
        checked = checkState.value,
        onCheckedChange = { checkState.value = it; if(checkState.value) membersWhoPay.add(member) else membersWhoPay.remove(member)  }
    )
}



fun editExpenseLine(id : String, name : String, amount : String, payer : String, membersWhoPay : SnapshotStateList<String>, expenseNavControl: NavController){

    val firestore = Firebase.firestore.collection("expenses").document(id)

    firestore.update("name",name)
    firestore.update("amount",amount)
    firestore.update("payer",payer)
    firestore.update("rest",membersWhoPay)
        .addOnSuccessListener {
            Log.d("Edit expense", id)
            expenseNavControl.navigate("group")
        }
}

fun getGroupMembers(groupid : String, listmembers : MutableState<List<String>>){
    Firebase.firestore.collection("groups")
        .document(groupid)
        .get()
        .addOnSuccessListener {
            var members = mutableListOf<String>()
            val list = it.get("members") as? List<String>
            list!!.forEach { element ->
                members.add(element.substringBefore("@"))
            }

            listmembers.value = members
        }
}
