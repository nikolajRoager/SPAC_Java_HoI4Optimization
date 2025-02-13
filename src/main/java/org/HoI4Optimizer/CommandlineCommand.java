package org.HoI4Optimizer;

import com.diogonunes.jcolor.Attribute;
import net.bytebuddy.utility.nullability.MaybeNull;
import net.bytebuddy.utility.nullability.NeverNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;

public class CommandlineCommand
{
    /// What is this command?
    private final String command;
    /// What does this command do in one line?
    private final String description;

    public static class Argument
    {
        public String argName;
        public String description;

        public enum type
        {
            /// Integer argument, need command=5 or similar
            Integer,
            /// Boolean argument, need command=true or command=false
            Boolean,
            /// Floating point, need command=1.4 or similar
            Float,
            /// Boolean, if the argument is there it is true, if not it is false
            Flag,
            /// The argument is a string, the StringValue is our only value
            String
        }
        @NeverNull
        public type myType;

        /// Has default argument
        public boolean isOptional;

        ///The default argument, must correspond with the type
        public String defaultArgument;

        ///The string inserted, if the type is a string then this is our value
        public String stringValue;

        /// A single argument for a commandline command, which can be compared to a list of strings, and parse out its values
        /// @param argName the name of the argument, for example "state", or "days", or whatever else
        /// @param description a description of what the command does, used for printing an explanatiohn
        /// @param type What type this argument is, should be either Integer, Float for numeric arguments, Boolean for boolean arguments (like showFactories true), Flag for flags (similar to booleans, but mere presence of argument is read as true or false), or String which accepts any string
        /// @param defaultArgument the string we read for this argument, if no argument matches argName, may be null only if isOptional = false, has no effects if isOptional=false
        /// @param isOptional this argument is not required in the command, defaultArgument will be returned if not present
        /// @throws IllegalArgumentException if default argument does not match the type, or is null while isOptional=true
        Argument(@NeverNull String argName, String description, @NeverNull type type, boolean isOptional, @MaybeNull String defaultArgument) throws IllegalArgumentException
        {
            this.argName = argName;
            this.myType=type;
            this.description=description;
            this.isOptional=isOptional;
            //Flags are special, they are true if there, false otherwise
            if (type== Argument.type.Flag)
            {
                this.isOptional=true;
                this.defaultArgument="false";
            }
            else if (defaultArgument==null && isOptional)
                throw new IllegalArgumentException("Default argument for "+argName+"is null or empty, while argument is optional");
            else if (!isOptional)//Ignore default argument, if the argument is not optional
                this.defaultArgument="null";
            else//implicitly this is: if (isOptional && defaultArgument!=null)
                switch(myType)
                {
                    case Integer, Float -> {
                        //Verify that the defaultArgument is sane
                        if (!StringUtils.isNumeric(defaultArgument))
                            throw new IllegalArgumentException("Default for "+argName+" argument must be a number");
                        //If this an integer, make sure to cast the default argument to an integer, it should not throw errors since we checked it is numeric
                        if (myType == Argument.type.Integer)
                            this.defaultArgument = Integer.toString((int)Double.parseDouble(defaultArgument));
                        else//Just read the argument, it is clearly a float
                            this.defaultArgument = defaultArgument;
                    }
                    case Boolean-> {
                        //Check that the default argument is a boolean
                        if (defaultArgument.equalsIgnoreCase("true") || defaultArgument.equalsIgnoreCase("1"))
                            this.defaultArgument = Boolean.toString(true);
                        else if (defaultArgument.equalsIgnoreCase("false") || defaultArgument.equalsIgnoreCase("0"))
                            this.defaultArgument = Boolean.toString(false);
                        else
                            throw new IllegalArgumentException("Default argument for "+argName+"must be a boolean");
                    }
                    case String -> this.defaultArgument = defaultArgument;
                }
        }
    }

    List<Argument> args;
    public CommandlineCommand(String c, String d,List<Argument> argumentList)
    {
        command=c.trim();
        description=d;
        args=argumentList;
    }

    final static Attribute[] command_colors = {Attribute.RED_BACK(),Attribute.GREEN_BACK(),Attribute.BLUE_BACK(),Attribute.YELLOW_BACK()};
    public void print(int commandI)
    {
        int MaxNameLength =0;
        for (var a : args)
        {
            MaxNameLength=Math.max(MaxNameLength,a.argName.length());
        }

        //First print command
        System.out.print(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+" "+ colorize(command,Attribute.ITALIC(),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        //Then all arguments

        Attribute[] colors = {Attribute.RED_TEXT(),Attribute.GREEN_TEXT(),Attribute.BLUE_TEXT(),Attribute.YELLOW_TEXT()};
        int argi=0;
        for (var a : args)
        {
            System.out.print(colorize( " "+a.argName,colors[argi], a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),Attribute.ITALIC(),Attribute.BOLD() ));
            switch (a.myType)
            {
                case Flag -> System.out.print(colorize(" ",colors[argi],Attribute.ITALIC(), a.isOptional?Attribute.NONE():Attribute.UNDERLINE()));//Just a flag, no need to include anything
                case Float -> System.out.print(colorize(" "+(a.isOptional?a.defaultArgument :"Float"),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
                case Integer -> System.out.print(colorize(" "+(a.isOptional?a.defaultArgument :"Integer "),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
                case String -> System.out.print(colorize(" "+(a.isOptional?a.defaultArgument :"String "),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
                case Boolean -> System.out.print(colorize(" "+(a.isOptional?a.defaultArgument :"True/False "),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
            }
            argi=(argi+1)%colors.length;
        }
        //Finish line
        System.out.println();
        //Now print description
        System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+"\t\t"+ colorize(description,Attribute.ITALIC(),Attribute.BRIGHT_WHITE_TEXT()));
        if (!args.isEmpty())
        {
            argi=0;
            System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+colorize("\t\tArguments:",Attribute.WHITE_TEXT()));
            for (var a : args) {
                System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+"\t\t\t"+colorize( " "+a.argName +org.apache.commons.lang3.StringUtils.repeat(' ',MaxNameLength-a.argName.length())+" "+(a.isOptional?"(optional) ":"           ")+(a.myType)+org.apache.commons.lang3.StringUtils.repeat(' ',7-a.myType.toString().length())+": "+a.description+" ",colors[argi], a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),Attribute.ITALIC(),Attribute.BOLD() ));
                argi=(argi+1)%colors.length;
            }
        }
        System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI]));
        ++commandI;
    }

    /// Check if the strings match the command
    /// @param strings the list of strings from the user
    /// @return  returns a list of arguments as strings (after checking that they match the type!), returns null if not matching
    /// @throws IllegalArgumentException if the strings do not match the type of the argument
    public String[] match(List<String> strings) throws IllegalArgumentException
    {
        if (strings.isEmpty())
            return null;

        if (strings.getFirst().equalsIgnoreCase(command))
        {
            //Check if any argument mis-matches
            String[] out = new String[args.size()];
            for (int i = 0; i< args.size(); ++i)
            {
                //Index in strings
                int j;
                //If there is only one arg, we can leave it out
                if (strings.size()==2 && args.size()==1)
                {
                    //i=0; this is always true
                    j=0;
                }
                else {
                    for (j = 0; j < strings.size(); ++j) {
                        if (strings.get(j).equalsIgnoreCase(args.get(i).argName)) {
                            break;
                        }
                    }
                }

                //Not found, check if it is optional
                if (j==strings.size())
                {
                    if (args.get(i).isOptional)
                    {
                        out[i]=args.get(i).defaultArgument;
                    }
                    else
                        throw new IllegalArgumentException("Non optional argument "+args.get(i).argName+" missing");
                }
                else//Found
                {
                    //if this is a flag, just set it
                    if (args.get(i).myType== Argument.type.Flag)
                        out[i]="true";
                    else//Otherwise check the value
                    {
                        if (j+1< strings.size())
                        {
                           //Look for true or false
                           if (args.get(i).myType== Argument.type.Boolean)
                           {
                               if (strings.get(j+1).equalsIgnoreCase("true") || strings.get(j+1).equalsIgnoreCase("1"))
                               {
                                   out[i]="true";
                               } else if (strings.get(j+1).equalsIgnoreCase("false") || strings.get(j+1).equalsIgnoreCase("0"))
                               {
                                   out[i]="false";
                               } else
                                   throw new IllegalArgumentException("Argument "+args.get(i).argName+" ("+strings.get(j+1)+") is not a boolean type");
                           }
                           else if (args.get(i).myType== Argument.type.String)
                           {
                               //Strings are good
                               out[i] = strings.get(j+1);
                           }
                           else
                           {
//                               out[i]=Double.parseDouble(strings.get(j+1));
                               //Verify that the argument is sane
                               if (!StringUtils.isNumeric(strings.get(j+1)))
                                   throw new IllegalArgumentException("Argument "+args.get(i).argName+" ("+strings.get(j+1)+") is not numeric");
                               //If this an integer, make sure to cast the default argument to an integer, it should not throw errors since we checked it is numeric
                               if (args.get(i).myType == Argument.type.Integer)
                                   out[i] = Integer.toString((int)Double.parseDouble(strings.get(j+1)));
                               else//Just read the argument, it is clearly a float
                                   out[i] = strings.get(j+1);

                           }
                        }
                        else
                        {
                            //The user should know this
                            throw new IllegalArgumentException("Value for argument "+args.get(i).argName+" missing");
                        }
                    }
                }
            }

            //Match, may be {} if there were no arguments
            return out;
        }
        else
            return null;
    }
};
