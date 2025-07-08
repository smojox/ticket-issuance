package com.ceo.ticketissuance.presentation.ui.ticketissuance

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ceo.ticketissuance.domain.usecase.TicketGenerationResult
import java.time.format.DateTimeFormatter

@Composable
fun TicketPreview(
    ticketResult: TicketGenerationResult,
    onNavigateToHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ticket Generated Successfully",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ticket Number: ${ticketResult.ticket.ticketNumber}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        item {
            TmaTicketCard(ticketResult = ticketResult)
        }
        
        item {
            Button(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Ticket History")
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TmaTicketCard(ticketResult: TicketGenerationResult) {
    val ticket = ticketResult.ticket
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PENALTY CHARGE NOTICE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Traffic Management Act 2004",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ticket Number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Ticket Number: ${ticket.ticketNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vehicle Details
            TicketSection(title = "VEHICLE DETAILS") {
                TicketRow("Registration Number", ticket.vrm)
                TicketRow("Make", ticketResult.vehicleMake?.name ?: "Unknown")
                TicketRow("Model", ticketResult.vehicleModel?.name ?: "Unknown")
                TicketRow("Colour", ticket.color)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location Details
            TicketSection(title = "LOCATION DETAILS") {
                TicketRow("Street", "${ticketResult.street.name} (${ticketResult.street.areaCode})")
                TicketRow("Date of Issue", ticket.issueTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                TicketRow("Time of Issue", ticket.issueTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Contravention Details
            TicketSection(title = "CONTRAVENTION DETAILS") {
                TicketRow("Code", ticketResult.contravention.code)
                TicketRow("Description", ticketResult.contravention.description)
                TicketRow("Observation Period", "${ticketResult.contravention.observationPeriod} minutes")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Penalty Details
            TicketSection(title = "PENALTY DETAILS") {
                TicketRow("Penalty Amount", "£${ticket.penaltyAmount}")
                TicketRow("Early Payment Discount", "£${ticket.discountAmount}")
                TicketRow("Discounted Amount", "£${ticket.discountAmount}")
                Text(
                    text = "Early payment discount applies if paid within 14 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Officer Details
            TicketSection(title = "OFFICER DETAILS") {
                TicketRow("Officer Name", ticketResult.officerName)
                TicketRow("Badge Number", ticketResult.badgeNumber)
                if (ticketResult.officerSignature != null) {
                    TicketRow("Signature", "✓ Captured")
                }
            }
            
            if (ticket.notes?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                TicketSection(title = "ADDITIONAL NOTES") {
                    Text(
                        text = ticket.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer
            Divider(color = Color.Gray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "This notice is served under the Traffic Management Act 2004. " +
                        "You have the right to make representations or to appeal this penalty charge notice.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TicketSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun TicketRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}