package br.edu.scl.ifsp.sdm.intents

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.scl.ifsp.sdm.intents.Extras.PARAMETER_EXTRA
import br.edu.scl.ifsp.sdm.intents.databinding.ActivityParameterBinding

class ParameterActivity : AppCompatActivity() {
    private val activityParameterBinding: ActivityParameterBinding by lazy {
        ActivityParameterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityParameterBinding.root)
        setSupportActionBar(activityParameterBinding.toolbarIn.toolbar)
        supportActionBar?.subtitle = localClassName

        // tratamento para receber os dados da MainActivity por meio da chave PARAMETER_EXTRA
        intent.getStringExtra(PARAMETER_EXTRA)?.let { // função de escopo let
            activityParameterBinding.parameterEt.setText(it)
        }


        // tratamento para retornar as alterações realizadas no editText parameterEt
        activityParameterBinding.apply { // apply -> função de escopo da activity activityParameterBinding

            // tratamento do evento click do botão returnCloseBt
            returnCloseBt.setOnClickListener{
                // cria uma intent vazia (sem action) para retorna os dados para a MainActivity
                val resultIntent = Intent().apply {
                    // pega os valores armazenados no editText parameterEt, converter para string e armazena na chave PARAMETER_EXTRA
                    putExtra(PARAMETER_EXTRA, parameterEt.text.toString())
                }
                // retorna os valores do parameterEt através da intent resultIntent e exibe o valor pré-definido da linguagem RESULT_OK
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}