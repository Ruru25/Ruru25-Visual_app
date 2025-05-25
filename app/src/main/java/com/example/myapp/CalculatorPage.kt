package com.example.myapp

import android.content.Intent
import android.widget.Button
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalculatorPage : AppCompatActivity() {

    private lateinit var viewTable: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator_page)

        viewTable = findViewById(R.id.viewTable)

        findViewById<Button>(R.id.buttonZero).setOnClickListener { appendToResult("0") }
        findViewById<Button>(R.id.buttonOne).setOnClickListener { appendToResult("1") }
        findViewById<Button>(R.id.buttonTwo).setOnClickListener { appendToResult("2") }
        findViewById<Button>(R.id.buttonThree).setOnClickListener { appendToResult("3") }
        findViewById<Button>(R.id.buttonFour).setOnClickListener { appendToResult("4") }
        findViewById<Button>(R.id.buttonFive).setOnClickListener { appendToResult("5") }
        findViewById<Button>(R.id.buttonSix).setOnClickListener { appendToResult("6") }
        findViewById<Button>(R.id.buttonSeven).setOnClickListener { appendToResult("7") }
        findViewById<Button>(R.id.buttonEight).setOnClickListener { appendToResult("8") }
        findViewById<Button>(R.id.buttonNine).setOnClickListener { appendToResult("9") }
        findViewById<Button>(R.id.buttonPlus).setOnClickListener { appendToResult("+") }
        findViewById<Button>(R.id.buttonMinus).setOnClickListener { appendToResult("-") }
        findViewById<Button>(R.id.buttonMultiple).setOnClickListener { appendToResult("*") }
        findViewById<Button>(R.id.buttonDivision).setOnClickListener { appendToResult("/") }

        findViewById<Button>(R.id.buttonDelete).setOnClickListener { deleteResult() }

        findViewById<Button>(R.id.buttonEqual).setOnClickListener { getEqual() }

        findViewById<Button>(R.id.B_back).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun appendToResult(value: String) {
        viewTable.text = "${viewTable.text}$value"
    }

    private fun deleteResult() {
        viewTable.text = ""
    }

    private fun getEqual() {
        var operator = ' '
        var a = ""
        var b = ""
        val text = viewTable.text.toString()
        var foundOperator = false

        for (i in text.indices) {
            if (text[i] == '+' || text[i] == '-' || text[i] == '*' || text[i] == '/') {
                operator = text[i]
                foundOperator = true
                continue
            }


            if (!foundOperator) {
                try {
                    a += text[i]
                }catch (e: NumberFormatException){
                    viewTable.text = "Неверный формат"
                }
            } else {
                b += text[i]
            }
        }

        if (!foundOperator) {
            viewTable.text = "Нет оператора"
            return
        }
        if (b == "") {
            viewTable.text = "Нет вторго числа"
            return
        }

        try {
            val first = a.toInt()
            val second = b.toInt()

            if (operator == '/' && second == 0) {
                viewTable.text = "Деление на ноль"
                return
            }

            val output: Int = when (operator) {
                '+' -> Math.addExact(first, second)
                '-' -> Math.subtractExact(first, second)
                '*' -> Math.multiplyExact(first, second)
                else -> throw Error("Ошибка")
            }
            viewTable.text = output.toString()
        } catch (e: ArithmeticException) {
            viewTable.text = "Переполнение"
        } catch (e: NumberFormatException) {
            viewTable.text = "Неверный формат, очистете поле ввода"
        } catch (e: Exception) {
            viewTable.text = "Ошибка"
        }
    }
}