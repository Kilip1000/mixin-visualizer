package dev.wvr.mixinvisualizer.logic.handlers

import dev.wvr.mixinvisualizer.logic.asm.AsmHelper
import dev.wvr.mixinvisualizer.logic.util.AnnotationUtils
import dev.wvr.mixinvisualizer.logic.util.CodeGenerationUtils
import dev.wvr.mixinvisualizer.logic.util.SliceHelper
import dev.wvr.mixinvisualizer.logic.util.TargetFinderUtils
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ModifyReturnValueHandler : MixinHandler {
    override fun canHandle(annotationDesc: String): Boolean = annotationDesc.contains("ModifyReturnValue")

    override fun handle(
        targetClass: ClassNode,
        mixinClass: ClassNode,
        sourceMethod: MethodNode,
        annotation: AnnotationNode
    ) {
        val targets = AnnotationUtils.getListValue(annotation, "method")
        var atValue = AnnotationUtils.getAtValue(annotation, "at")

        if (atValue.isEmpty()) atValue = "RETURN"

        for (ref in targets) {
            val targetMethod = TargetFinderUtils.findTargetMethodLike(targetClass, ref) ?: continue
            val (startNode, endNode) = SliceHelper.getSliceRange(targetClass, targetMethod, annotation)

            val injectionData = CodeGenerationUtils.prepareCode(
                sourceMethod,
                mixinClass.name,
                targetClass.name,
                targetMethod,
                isRedirect = false
            )
            when (atValue) {
                "TAIL", "RETURN" -> {
                    val iter = targetMethod.instructions.iterator()
                    while (iter.hasNext()) {
                        val insn = iter.next()
                        if (insn.opcode in Opcodes.IRETURN..Opcodes.RETURN) {
                            val returnInstruction = insn
                            val lvIndex = targetMethod.maxLocals
                            targetMethod.maxLocals += sourceMethod.maxLocals


                            val returnValueStoreAndLoader = InsnList()

                            //store old returnvalue
                            when (returnInstruction.opcode) {
                                Opcodes.IRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.ISTORE, lvIndex))
                                Opcodes.LRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.LSTORE, lvIndex))
                                Opcodes.FRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.FSTORE, lvIndex))
                                Opcodes.DRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.DSTORE, lvIndex))
                                Opcodes.ARETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.ASTORE, lvIndex))
                            }

                            //also load this on stack
                            returnValueStoreAndLoader.add(VarInsnNode(Opcodes.ALOAD, 0))


                            //find returnvalue to be able to assign a variable
                            when (returnInstruction.opcode) {
                                Opcodes.IRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.ILOAD, lvIndex))
                                Opcodes.LRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.LLOAD, lvIndex))
                                Opcodes.FRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.FLOAD, lvIndex))
                                Opcodes.DRETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.DLOAD, lvIndex))
                                Opcodes.ARETURN -> returnValueStoreAndLoader.add(VarInsnNode(Opcodes.ALOAD, lvIndex))
                            }


                            targetMethod.instructions.insertBefore(returnInstruction, returnValueStoreAndLoader)

                            val map = HashMap<LabelNode, LabelNode>()
                            val code = cloneInstructions(injectionData.instructions, map)
                            val tcbs = AsmHelper.cloneTryCatchBlocks(injectionData.tryCatchBlocks, map)

                            targetMethod.instructions.insertBefore(insn, code)
                            targetMethod.tryCatchBlocks.addAll(tcbs)
                        }
                    }
                }
            }


        }
    }


    fun cloneInstructions(list: InsnList, map: MutableMap<LabelNode, LabelNode>? = null): InsnList {
        val clone = InsnList()
        val labels = map ?: mutableMapOf()

        var p = list.first
        while (p != null) {
            if (p is LabelNode) {
                if (!labels.containsKey(p)) {
                    labels[p] = LabelNode()
                }
            }
            p = p.next
        }

        p = list.first
        while (p != null) {
            clone.add(p.clone(labels))
            p = p.next
        }
        return clone
    }
}
