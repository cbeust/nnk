package com.beust.nnk

data class NonLinearity(
        val activate :((Float) -> Float),
        val activateDerivative:((Float) -> Float)
)

object NonLinearities{
    private val CommonNonLinearity= HashMap<String, NonLinearity> ()
    init{
        CommonNonLinearity.put("TANH", NonLinearity({ x->Math.tanh(x.toDouble()).toFloat()},{ x->(1.0f - x * x)}))
        CommonNonLinearity.put("RELU", 
                NonLinearity(
                        { x-> if (x>0){x}else 0f}, 
                        { x->if (x>0){1f}else 0f}
                )
        )
        CommonNonLinearity.put("LEAKYRELU",
                NonLinearity(
                        { x-> if (x>0){x}else {0.01f*x}},
                        { x->if (x>0){1f}else {0.01f}}
                )
        )
    }

    fun get(nonLinearityName: String): NonLinearity {
        return CommonNonLinearity.get(nonLinearityName.toUpperCase()) ?: throw Exception("no nonLinearity are called \"$nonLinearityName\"")
    }

}
