package com.julianpeeters.caseclass.generator

import scala.reflect.runtime.universe._
import org.objectweb.asm._
import Opcodes._

case class Equals(cw: ClassWriter, var mv: MethodVisitor, caseClassName: String, fieldData: List[EnrichedField]) {

  def dump = {

    val userDefinedTypes = ClassStore.generatedClasses.keys.toList
    mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
    mv.visitCode(); //if there's a typeOf[Nothing] then drop all value members after the first typeOf[Nothing].
    val fields = {
      if (fieldData.map(n => n.fieldType).contains(typeOf[Nothing])) fieldData.take(fieldData.indexWhere(m => m.fieldType =:= typeOf[Nothing]) + 1); 
      else fieldData
    }
    var l0: Label = null

    if (fieldData.length > 0) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      l0 = new Label();

      if (fieldData.map(n => n.fieldType).forall(t => List(typeOf[Nothing]).contains(t))) { //if all the valueMembers are typeOf[Nothing]
        mv.visitJumpInsn(IF_ACMPNE, l0);
        mv.visitInsn(ICONST_1);
        val nothingLabel = new Label();
        mv.visitJumpInsn(GOTO, nothingLabel);
        mv.visitLabel(l0);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      }
      else {
        mv.visitJumpInsn(IF_ACMPEQ, l0);
      }
    }

    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ASTORE, 2);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitTypeInsn(INSTANCEOF, caseClassName);
    val l1 = new Label();
    mv.visitJumpInsn(IFEQ, l1);

    mv.visitInsn(ICONST_1);
    mv.visitVarInsn(ISTORE, 3);
    val l2 = new Label();
    mv.visitJumpInsn(GOTO, l2);
    mv.visitLabel(l1);
    mv.visitFrame(Opcodes.F_APPEND, 1, Array[Object] ("java/lang/Object"), 0, null);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 3);
    mv.visitLabel(l2);
    mv.visitFrame(Opcodes.F_APPEND, 1, Array[Object] (Opcodes.INTEGER), 0, null);
    mv.visitVarInsn(ILOAD, 3);
    val l3 = new Label();
    mv.visitJumpInsn(IFEQ, l3);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, caseClassName);

    if (fieldData.length > 0) mv.visitVarInsn(ASTORE, 4);

    var valueMembersGOTOLabel: Label = null
    var penultimateLabel: Label = null
    var ultimateLabel: Label = null

    fields.foreach(valueMember => {
      valueMember.fieldType match {
        case a if (a =:= typeOf[Boolean] |
          a =:= typeOf[Byte] | 
          a =:= typeOf[Char] | 
          a =:= typeOf[Short] | 
          a =:= typeOf[Int] ) => {

          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
          mv.visitVarInsn(ALOAD, 4);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
          if (fieldData.indexOf(valueMember) == 0) valueMembersGOTOLabel = new Label();
          mv.visitJumpInsn(IF_ICMPNE, valueMembersGOTOLabel);
        }

        case x if (x =:= typeOf[Double] | x =:= typeOf[Float] | x =:= typeOf[Long] ) => {  
          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
          mv.visitVarInsn(ALOAD, 4);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
          if (valueMember.fieldType =:= typeOf[Double]) mv.visitInsn(DCMPL);
          else if (valueMember.fieldType =:= typeOf[Float]) mv.visitInsn(FCMPL);
          else if (valueMember.fieldType =:= typeOf[Long]) mv.visitInsn(LCMP);

          if (fieldData.indexOf(valueMember) == 0) valueMembersGOTOLabel = new Label();
          mv.visitJumpInsn(IFNE, valueMembersGOTOLabel);
        }

        case x @ TypeRef(pre, symbol, args) if (
          x =:= typeOf[String] |
          x =:= typeOf[Null] |
          x =:= typeOf[Unit] |
          (x <:< typeOf[List[Any]] && args.length == 1 )|
          (x <:< typeOf[Stream[Any]] && args.length == 1 ) |
          (x <:< typeOf[Option[Any]] && args.length == 1 ) ) => { 
          if (valueMember.fieldType =:= typeOf[Unit]) {
            mv.visitFieldInsn(GETSTATIC, "scala/runtime/BoxedUnit", "UNIT", "Lscala/runtime/BoxedUnit;");
            mv.visitFieldInsn(GETSTATIC, "scala/runtime/BoxedUnit", "UNIT", "Lscala/runtime/BoxedUnit;");
          }
          else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
            if (valueMember.fieldType =:= typeOf[Null]) {
              mv.visitInsn(POP);
              mv.visitInsn(ACONST_NULL);
            }
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
            if (valueMember.fieldType =:= typeOf[Null]) {
              mv.visitInsn(POP);
              mv.visitInsn(ACONST_NULL);
            }
          }
          mv.visitVarInsn(ASTORE, 5);
          mv.visitInsn(DUP);
          val l4 = new Label();
          mv.visitJumpInsn(IFNONNULL, l4);
          mv.visitInsn(POP);
          mv.visitVarInsn(ALOAD, 5);
          val l5 = new Label();
          mv.visitJumpInsn(IFNULL, l5);
          if (fieldData.indexOf(valueMember) == 0) valueMembersGOTOLabel = new Label();
          mv.visitJumpInsn(GOTO, valueMembersGOTOLabel);
          mv.visitLabel(l4);
          if (valueMember.fieldType =:= typeOf[Null]) {
            mv.visitFrame(Opcodes.F_FULL, 6, Array[Object] (caseClassName, "java/lang/Object", "java/lang/Object", Opcodes.INTEGER, caseClassName, Opcodes.NULL), 1, Array[Object] (Opcodes.NULL));
          }
          else {
            mv.visitFrame(Opcodes.F_FULL, 6, Array[Object] (caseClassName, "java/lang/Object", "java/lang/Object", Opcodes.INTEGER, caseClassName, valueMember.typeData.typeDescriptor.drop(1).dropRight(1)), 1, Array[Object] (valueMember.typeData.typeDescriptor.drop(1).dropRight(1)));
          }
          mv.visitVarInsn(ALOAD, 5);
          mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
          mv.visitJumpInsn(IFEQ, valueMembersGOTOLabel);
          mv.visitLabel(l5);
          mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }

        case n if n =:= typeOf[Any] | n =:= typeOf[AnyRef] | n =:= typeOf[Object] => {
          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()Ljava/lang/Object;");
          mv.visitVarInsn(ALOAD, 4);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()Ljava/lang/Object;");
          mv.visitMethodInsn(INVOKESTATIC, "scala/runtime/BoxesRunTime", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
          if (fieldData.indexOf(valueMember) == 0) valueMembersGOTOLabel = new Label();
          mv.visitJumpInsn(IFEQ, valueMembersGOTOLabel);
        }
        case n if n =:= typeOf[Nothing] => {
          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()Lscala/runtime/Nothing$;");
          mv.visitInsn(ATHROW);
        }

        case x @ TypeRef(pre, symbol, args) => {
          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);
          mv.visitVarInsn(ALOAD, 4);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, valueMember.fieldName, "()" + valueMember.typeData.typeDescriptor);

          mv.visitVarInsn(ASTORE, 5);
          mv.visitInsn(DUP);
          val l4 = new Label();
          mv.visitJumpInsn(IFNONNULL, l4);
          mv.visitInsn(POP);
          mv.visitVarInsn(ALOAD, 5);
          val l5 = new Label();
          mv.visitJumpInsn(IFNULL, l5);
          if (fieldData.indexOf(valueMember) == 0) valueMembersGOTOLabel = new Label();
          mv.visitJumpInsn(GOTO, valueMembersGOTOLabel);
          mv.visitLabel(l4);

          val namespace = caseClassName.takeWhile(c => (c != '/'))
          val name = valueMember.typeData.typeDescriptor.drop(1).dropRight(1)

          mv.visitFrame(Opcodes.F_FULL, 6, Array[Object] (caseClassName, "java/lang/Object", "java/lang/Object", Opcodes.INTEGER, caseClassName, name), 1, Array[Object] (name));
          mv.visitVarInsn(ALOAD, 5);
          mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
          mv.visitJumpInsn(IFEQ, valueMembersGOTOLabel);
          mv.visitLabel(l5);
          mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        case _ => error("Could not generate equals method: unsupported type")
      }
    })

    //do the following unless there is was typeOf[Nothing] type and we broke out with a ATHROW instead
    if (!fieldData.map(n => n.fieldType).contains(typeOf[Nothing])) mv.visitVarInsn(ALOAD, 4);
    //if all value members are of type typeOf[Nothing], skip the canEqual portion and go to the final portion
    if (!fieldData.map(n => n.fieldType).forall(t => List(typeOf[Nothing]).contains(t))) {
      fields.contains(typeOf[Nothing]) match {
        case true => { //if there is a typeOf[Nothing] type, then it will be the last member, canEqual is skipped
          mv.visitLabel(valueMembersGOTOLabel);
          mv.visitFrame(Opcodes.F_APPEND, 1, Array[Object] (caseClassName), 0, null);
          mv.visitInsn(ICONST_0);
          mv.visitJumpInsn(IFEQ, l3);
          mv.visitLabel(l0);
          mv.visitFrame(Opcodes.F_CHOP, 3, null, 0, null);
        }
        case false => { //there is not a Nothing type in the record 
          mv.visitVarInsn(ALOAD, 0);
          mv.visitMethodInsn(INVOKEVIRTUAL, caseClassName, "canEqual", "(Ljava/lang/Object;)Z");
          mv.visitJumpInsn(IFEQ, valueMembersGOTOLabel);
          mv.visitInsn(ICONST_1);
          penultimateLabel = new Label();
          mv.visitJumpInsn(GOTO, penultimateLabel);
          mv.visitLabel(valueMembersGOTOLabel);

          //if all value members are from this list, then:
          if (fieldData.map(n => n.fieldType).forall(t => { 
            List(typeOf[Any], 
                 typeOf[AnyRef], 
                 typeOf[Boolean], 
                 typeOf[Byte], 
                 typeOf[Char], 
                 typeOf[Int], 
                 typeOf[Double], 
                 typeOf[Float], 
                 typeOf[Long], 
                 typeOf[Short], 
                 typeOf[Object]).contains(t)}
              )) { //if all field types are types on this list 

            mv.visitFrame(Opcodes.F_APPEND, 1, Array[Object] (caseClassName), 0, null);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(penultimateLabel);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, Array[Object] (Opcodes.INTEGER));
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_CHOP, 3, null, 0, null);
          }
          if ((List(typeOf[String].erasure, 
                    typeOf[Unit].erasure,
                    typeOf[Option[Any]].erasure,
                    typeOf[List[Any]].erasure,
                    typeOf[Null]
               ) ::: userDefinedTypes).contains(fieldData.head.fieldType.erasure)) {

            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(penultimateLabel);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, Array[Object] (Opcodes.INTEGER));
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_FULL, 2, Array[Object] (caseClassName, "java/lang/Object"), 0, Array[Object] ());
          }
          else if (List(typeOf[Any], 
            typeOf[AnyRef], 
            typeOf[Boolean], 
            typeOf[Byte], 
            typeOf[Char], 
            typeOf[Int], 
            typeOf[Double], 
            typeOf[Float], 
            typeOf[Long], 
            typeOf[Short], 
            typeOf[Object]).contains(fieldData.head.fieldType)) {
            mv.visitInsn(ICONST_0);
            mv.visitLabel(penultimateLabel);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, Array[Object] (Opcodes.INTEGER));
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_CHOP, 3, null, 0, null);
          }
        }
      }
      mv.visitInsn(ICONST_1);
      ultimateLabel = new Label();
      mv.visitJumpInsn(GOTO, ultimateLabel);
      mv.visitLabel(l3);
      mv.visitFrame(Opcodes.F_APPEND, 2, Array[Object] ("java/lang/Object", Opcodes.INTEGER), 0, null);
    }
    else {
      mv.visitLabel(l3);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

    mv.visitInsn(ICONST_0);
    mv.visitLabel(ultimateLabel);
    mv.visitFrame(Opcodes.F_FULL, 2, Array[Object] (caseClassName, "java/lang/Object"), 1, Array[Object] (Opcodes.INTEGER));
    mv.visitInsn(IRETURN);
    mv.visitMaxs(2, 6);
    mv.visitEnd();
  }
}
