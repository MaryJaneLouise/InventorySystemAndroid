/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.item

import androidx.annotation.StringRes
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(topBar = {
        InventoryTopAppBar(
            title = stringResource(ItemDetailsDestination.titleRes),
            canNavigateBack = true,
            navigateUp = navigateBack
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navigateToEditItem(uiState.value.itemDetails.id) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
                Icon(

                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_item_title),
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                Text(text = stringResource(R.string.edit_item_title), fontSize = 16.sp)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            }
        }
    }, modifier = modifier
    ) { innerPadding ->
        ItemDetailsBody(
            itemDetailsUiState = uiState.value,
            onSellItem = { viewModel.reduceQuantityByOne() },
            onAdd = { viewModel.addQuantityByOne() },
            onDelete = {
                // Note: If the user rotates the screen very fast, the operation may get cancelled
                // and the item may not be deleted from the Database. This is because when config
                // change occurs, the Activity will be recreated and the rememberCoroutineScope will
                // be cancelled - since the scope is bound to composition.
                coroutineScope.launch {
                    viewModel.deleteItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
            )
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String
) {
    val customButtonColors = ButtonDefaults.textButtonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = customButtonColors
    ) {
        Text(
            text = text
        )
    }
}

@Composable
private fun ItemDetailsBody(
    itemDetailsUiState: ItemDetailsUiState,
    onSellItem: () -> Unit,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        ItemDetails(
            item = itemDetailsUiState.itemDetails.toItem(), modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onSellItem,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                enabled = !itemDetailsUiState.outOfStock
            ) {
                Text(stringResource(R.string.sell))
            }

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(stringResource(R.string.add_stocks))
            }
        }
        CustomButton(
            onClick = { deleteConfirmationRequired = true },
            text = "Delete item"
        )

        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(onDeleteConfirm = {
                deleteConfirmationRequired = false
                onDelete()
            },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

fun Long.toLocalDateTime(): Date {
    return Date(this)
}

fun Date.toFormattedDateString(): String {
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.US)
    return dateFormatter.format(this)
}

@Composable
fun ItemDetails(
    item: Item, modifier: Modifier = Modifier
) {
    fun formatNumberWithThousandsSeparator(number: Int): String {
        val numberFormat: NumberFormat = DecimalFormat("#,###")
        return numberFormat.format(number)
    }
    val quantityFormatted = formatNumberWithThousandsSeparator(item.quantity)
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            ItemDetailsRow(
                labelResID = R.string.item_name,
                itemDetail = item.name,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                    .padding_medium))
            )
            if (item.quantity > 0) {
                if (item.quantity == 1) {
                    ItemDetailsRow(
                        labelResID = R.string.quantity_in_stock,
                        itemDetail = stringResource(R.string.one_stock, quantityFormatted),
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                            .padding_medium))
                    )
                } else {
                    ItemDetailsRow(
                        labelResID = R.string.quantity_in_stock,
                        itemDetail = stringResource(R.string.in_stock, quantityFormatted),
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                            .padding_medium))
                    )
                }
            } else {
                ItemDetailsRow(
                    labelResID = R.string.quantity_in_stock,
                    itemDetail = stringResource(R.string.out_stock),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                        .padding_medium))
                )
            }

            ItemDetailsRow(
                labelResID = R.string.price,
                itemDetail = item.formatedPrice(),
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                    .padding_medium))
            )
            if (item.date_added.toFormattedDateString() == item.date_updated.toFormattedDateString()) {
                ItemDetailsRow(
                    labelResID =  R.string.date_added,
                    itemDetail = item.date_added.toFormattedDateString(),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                        .padding_medium))
                )
            } else {
                ItemDetailsRow(
                    labelResID =  R.string.date_added,
                    itemDetail = item.date_added.toFormattedDateString(),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                        .padding_medium))
                )
                ItemDetailsRow(
                    labelResID =  R.string.date_updated,
                    itemDetail = item.date_updated.toFormattedDateString(),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen
                        .padding_medium))
                )
            }
        }
    }
}

@Composable
private fun ItemDetailsRow(
    @StringRes labelResID: Int, itemDetail: String, modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(labelResID),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = itemDetail,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, onDeleteCancel: () -> Unit, modifier: Modifier = Modifier
) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        })
}

@Preview(showBackground = true)
@Composable
fun ShowDeleteScreenPreview() {
    DeleteConfirmationDialog(onDeleteConfirm = { /*TODO*/ }, onDeleteCancel = { /*TODO*/ })
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsScreenPreview() {
    InventoryTheme {
        ItemDetailsBody(ItemDetailsUiState(
            outOfStock = true, itemDetails = ItemDetails(1, "Pen", "100", "2")
        ), onAdd = {}, onSellItem = {}, onDelete = {})
    }
}
