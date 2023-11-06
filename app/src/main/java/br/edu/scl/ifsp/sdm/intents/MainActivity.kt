package br.edu.scl.ifsp.sdm.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.Intent.ACTION_CHOOSER
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_PICK
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.EXTRA_TITLE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import br.edu.scl.ifsp.sdm.intents.Extras.PARAMETER_EXTRA
import br.edu.scl.ifsp.sdm.intents.databinding.ActivityMainBinding

// https://www.youtube.com/watch?v=-tjeunc_zuA

class MainActivity : AppCompatActivity() {

    /*
    // definição do PARAMETER_REQUEST_CODE necessário na forma LEGADA de solicitar a abertura de uma activity passando valores para ela por meio de uma intent
    companion object {
        private const val PARAMETER_REQUEST_CODE = 0
    }
    */

    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // variável do tipo ActivityResultLauncher necessária para a forma moderna de receber dados de outra activity (ParameterActivity) através de uma intent
    private lateinit var parameterArl: ActivityResultLauncher<Intent>

    // variável do tipo ActivityResultLauncher para tratamento da permissão de chamada
    private lateinit var callPhonePermissionArl: ActivityResultLauncher<String>

    // variável do tipo ActivityResultLauncher para acesso ao armazenametno externo
    private lateinit var pickImageActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbarIn.toolbar)
        supportActionBar?.subtitle = localClassName

        // forma MODERNA de receber dados de outra activity (ParameterActivity) através de uma intent
        parameterArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.getStringExtra(PARAMETER_EXTRA)?.also {
                    activityMainBinding.parameterTv.text = it
                }
            }
        }

        callPhonePermissionArl = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                // fazer chamada já que o usuário deu permissão de chamada para o aplicativo
                callPhone(call = true)
            } else {
                // usuário NÃO deu permissão de chamada para o aplicativo
                Toast.makeText(this,
                    getString(R.string.permission_required_to_call), Toast.LENGTH_SHORT).show()
            }
        }

        pickImageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            with (result) {
                if (resultCode == RESULT_OK) {
                    data?.data?.also { // primeiro data é a Intent e o segundo data é a URI do arquivo que foi selecionado
                        activityMainBinding.parameterTv.text = it.toString() // tratamento toString por se tratar de uma URI
                        startActivity(Intent(ACTION_VIEW).apply { data = it }) // cria uma Intent passando a URI da imagem por meio do parâmetro data armazenado na variável it para visualizá-la
                    }
                }
            }
        }

        activityMainBinding.apply {
            parameterBt.setOnClickListener {
                // cria uma intent EXPLÍCITA
                val parameterIntent = Intent(this@MainActivity, ParameterActivity::class.java).apply {
                    // define os parâmetros que serão repassados para o ParameterActivity
                    putExtra(PARAMETER_EXTRA, parameterTv.text)
                }

                // forma obsoleta (legada) de solicitar a abertura de uma activity passando valores para ela por meio de uma intent
                // startActivityForResult(parameterIntent, PARAMETER_REQUEST_CODE)

                // forma ATUAL de solicitar a abertura de uma activity passando valores para ela por meio de uma intent
                parameterArl.launch(parameterIntent)
            }
        }
    }

    /*

    // função OBSOLETA para receber os dados da activity ParameterActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PARAMETER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringExtra(PARAMETER_EXTRA)?.also {
                activityMainBinding.parameterTv.text = it
            }
        }
    }

    */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.openActivityMi -> {
                // cria de uma intent IMPLÍCITA
                val parameterIntent = Intent("OPEN_PARAMETER_ACTIVITY_ACTION").apply {
                    putExtra(PARAMETER_EXTRA, activityMainBinding.parameterTv.text)
                }
                parameterArl.launch(parameterIntent)
                true
            }
            R.id.viewMi -> {
                // abre um navegador com uma URL informada pelo usuário
                startActivity(browserIntent())
                true
            }
            R.id.callMi -> {
                // fazer uma chamada telefônica
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // verifica se a versão da API do Android que está executando o aplicativo é IGUAL ou MAIOR que 23
                    if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED) { //verifica se tem a permissão para fazer chamadas
                        // fazer a chamada através da função callPhone já que já foi tem a permissão para chamada
                        callPhone(call = true)
                    } else {
                        // solicitar a permissão
                        callPhonePermissionArl.launch(CALL_PHONE) // passa como parâmetro uma string com os dados da permissão que está sendo solicitada
                    }
                } else {
                    // versão da API do Android que está executando o aplicativo é MENOR que 23
                    callPhone(call = true)

                }
                true
            }

            R.id.dialMi -> {
                // abre o aplicativo discador para fazer uma chamada telefônica com os números informados no TextView parameterTv de maneira manual
                callPhone(call = false)
                true
            }

            R.id.pickMi -> {
                // pegar uma imagem da galeria
                // solicitar permissão de acesso ao armazenamento externo do dispositivo (arquivos e diretórios que não estão dentro da pasta do aplicativo)
                val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path // armazena o caminho do diretório onde estão salvas as fotos
                // cria uma Intent que acessa o diretório de fotos, seleciona uma foto através do ACTION_PICK e filtra o tipo de arquivo para que o usuário possa selecionar qual o aplicativo pode abrir tal arquivo
                pickImageActivityResultLauncher.launch(Intent(ACTION_PICK).apply { setDataAndType(Uri.parse(imageDir), "image/*") }) // método setDataAndType exige dois parâmetros: local onde estão armazenados os arquivos e o tipo dos arquivos
                true
            }
            R.id.chooserMi -> {
                // escolher um navegador através da criação de uma seletor de aplicativos
                // método ACTION_CHOOSER força a exibição de um seletor mesmo que o usuário já tenha escolhido um aplicativo padrão como navegador
                startActivity(
                    Intent(ACTION_CHOOSER).apply {
                        putExtra(EXTRA_TITLE, getString(R.string.choose_your_favorite_browser)) // define um título para a Intent
                        putExtra(EXTRA_INTENT, browserIntent()) // Intent interna que é carregada pela Intent do ACTION_CHOOSER a qual define o tipo de aplicativo que deve ser exibida para o usuário
                    }
                )

                true
            }
            else -> {
                false
            }
        }
    }

    private fun callPhone(call: Boolean) {
        // cria uma intent que recebe uma action do tipo ACTION_CALL e uma URI como um dado
        startActivity(
            Intent(if (call) ACTION_CALL else ACTION_DIAL).apply {
                "tel: ${activityMainBinding.parameterTv.text}".also {
                    data = Uri.parse(it)
                }
            }
        )
    }

    private fun browserIntent(): Intent {
        val url = Uri.parse(activityMainBinding.parameterTv.text.toString())
        // val browserIntent = Intent(ACTION_VIEW, url)
        return Intent(ACTION_VIEW, url) // é necessário importar a ação ACTION_VIEW
    }
}