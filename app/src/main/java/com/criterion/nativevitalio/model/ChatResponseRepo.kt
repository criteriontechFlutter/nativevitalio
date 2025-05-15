package com.critetiontech.ctvitalio.model


object ChatResponseRepo {
    fun getResponse(input: String): BotMessage {
        val lowercaseInput = input.lowercase()
        return when {
            lowercaseInput.contains("reset") || lowercaseInput.contains("start over") ->
                BotMessage("Conversation has been reset. How can I assist you today?", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme"))

            lowercaseInput.contains("vital") || lowercaseInput.contains("bp") || lowercaseInput.contains("heart rate") ->
                BotMessage("You can enter your vitals in the Vitals section.\nक्या आप जाना चाहेंगे?", false, listOf("Yes, open vitals", "No, thanks"))

            lowercaseInput.contains("vital details") || lowercaseInput.contains("normal values") ->
                BotMessage("Here are common vitals with normal and critical values:\n\n- BP: 120/80 mmHg (critical > 180/120)\n- HR: 60-100 bpm (critical <40 or >120)\n- SpO2: 95-100% (critical < 90%)\n- Temperature: 97.8-99.1°F (critical > 103°F)\n- Respiratory Rate: 12-20 breaths/min (critical <8 or >30)\n\nक्या आप vitals स्क्रीन खोलना चाहेंगे?", false, listOf("Open vitals", "No, go back"))

            lowercaseInput.contains("symptom") || lowercaseInput.contains("fever") || lowercaseInput.contains("pain") ->
                BotMessage("To record a symptom, go to the Symptoms tab.\nक्या आप अभी एक symptom रिकॉर्ड करना चाहेंगे?", false, listOf("Yes, open symptoms", "No"))

            lowercaseInput.contains("medicine") || lowercaseInput.contains("reminder") || lowercaseInput.contains("pill") ->
                BotMessage("Your medicine intake schedule and reminders are found under 'My Medicines'.\nक्या आप वहां जाना चाहेंगे?", false, listOf("Yes, open medicines", "No"))

            lowercaseInput.contains("fluid") || lowercaseInput.contains("drink") || lowercaseInput.contains("intake") ->
                BotMessage("Track your fluid intake in the Fluid section.\nक्या आप drink log करना चाहेंगे?", false, listOf("Yes, open fluids", "Maybe later"))

            lowercaseInput.contains("theme") || lowercaseInput.contains("dark mode") || lowercaseInput.contains("light mode") ->
                BotMessage("You can toggle Dark/Light mode in Settings > Theme.", false, listOf("Open settings"))

            lowercaseInput.contains("history") ->
                BotMessage("You can view your vitals, symptoms, fluids, and medicines history in their respective sections.", false, listOf("Open vitals history", "Open symptoms history"))

            lowercaseInput.contains("help") || lowercaseInput.contains("support") ->
                BotMessage("आप मुझसे vitals, symptoms, medicines, fluids या theme के बारे में पूछ सकते हैं। कैसे मदद कर सकता हूँ?", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme"))

            else -> BotMessage("मैं समझ नहीं पाया। कृपया दोबारा प्रयास करें या नीचे दिए विकल्पों में से चुनें:", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme","Start Over"))
        }
    }
}